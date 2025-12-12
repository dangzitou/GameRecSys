import os
from pyspark import SparkConf
from pyspark.sql import SparkSession
from pyspark.sql.functions import *
from pyspark.sql.types import *
from pyspark.ml.feature import BucketedRandomProjectionLSH
from pyspark.mllib.feature import Word2Vec
from pyspark.ml.linalg import Vectors
import random
from collections import defaultdict
import numpy as np
from pyspark.sql import functions as F


class UdfFunction:
    @staticmethod
    def sortF(game_list, timestamp_list):
        """
        sort by time and return the corresponding game sequence
        eg:
            input: game_list:[1,2,3]
                   timestamp_list:[1112486027,1212546032,1012486033]
            return [3,1,2]
        """
        pairs = []
        for m, t in zip(game_list, timestamp_list):
            pairs.append((m, t))
        # sort by time
        pairs = sorted(pairs, key=lambda x: x[1])
        return [x[0] for x in pairs]


def processItemSequence(spark, rawSampleDataPath):
    # rating data
    ratingSamples = spark.read.format("csv").option("header", "true").load(rawSampleDataPath)
    # ratingSamples.show(5)
    # ratingSamples.printSchema()
    sortUdf = udf(UdfFunction.sortF, ArrayType(StringType()))
    userSeq = ratingSamples \
        .where(F.col("rating") >= 3.5) \
        .groupBy("user_id") \
        .agg(sortUdf(F.collect_list("game_id"), F.collect_list("timestamp")).alias('gameIds')) \
        .withColumn("gameIdStr", array_join(F.col("gameIds"), " "))
    # userSeq.select("user_id", "gameIdStr").show(10, truncate = False)
    return userSeq.select('gameIdStr').rdd.map(lambda x: x[0].split(' '))


def embeddingLSH(spark, gameEmbMap):
    gameEmbSeq = []
    for key, embedding_list in gameEmbMap.items():
        embedding_list = [np.float64(embedding) for embedding in embedding_list]
        gameEmbSeq.append((key, Vectors.dense(embedding_list)))
    gameEmbDF = spark.createDataFrame(gameEmbSeq).toDF("gameId", "emb")
    bucketProjectionLSH = BucketedRandomProjectionLSH(inputCol="emb", outputCol="bucketId", bucketLength=0.1,
                                                      numHashTables=3)
    bucketModel = bucketProjectionLSH.fit(gameEmbDF)
    embBucketResult = bucketModel.transform(gameEmbDF)
    print("gameId, emb, bucketId schema:")
    embBucketResult.printSchema()
    print("gameId, emb, bucketId data result:")
    embBucketResult.show(10, truncate=False)
    print("Approximately searching for 5 nearest neighbors of the sample embedding:")
    sampleEmb = Vectors.dense(0.795, 0.583, 1.120, 0.850, 0.174, -0.839, -0.0633, 0.249, 0.673, -0.237)
    bucketModel.approxNearestNeighbors(gameEmbDF, sampleEmb, 5).show(truncate=False)


def trainItem2vec(spark, samples, embLength, embOutputPath, saveToRedis, redisKeyPrefix):
    # Set minCount to 1 because our dummy dataset is small and sparse
    word2vec = Word2Vec().setVectorSize(embLength).setWindowSize(5).setNumIterations(10).setMinCount(1)
    model = word2vec.fit(samples)
    
    # Try to find synonyms for a sample game ID (e.g. 105450) if it exists in the model
    try:
        synonyms = model.findSynonyms("105450", 20)
        for synonym, cosineSimilarity in synonyms:
            print(synonym, cosineSimilarity)
    except Exception as e:
        print(f"Could not find synonyms for sample game ID: {e}")

    embOutputDir = '/'.join(embOutputPath.split('/')[:-1])
    if not os.path.exists(embOutputDir):
        os.makedirs(embOutputDir)
    with open(embOutputPath, 'w') as f:
        for game_id in model.getVectors():
            vectors = " ".join([str(emb) for emb in model.getVectors()[game_id]])
            f.write(game_id + ":" + vectors + "\n")
    embeddingLSH(spark, model.getVectors())
    return model


def generate_pair(x):
    # eg:
    # watch sequence:['858', '50', '593', '457']
    # return:[['858', '50'],['50', '593'],['593', '457']]
    pairSeq = []
    previousItem = ''
    for item in x:
        if not previousItem:
            previousItem = item
        else:
            pairSeq.append((previousItem, item))
            previousItem = item
    return pairSeq


def generateTransitionMatrix(samples):
    pairSamples = samples.flatMap(lambda x: generate_pair(x))
    pairCountMap = pairSamples.countByValue()
    pairTotalCount = 0
    transitionCountMatrix = defaultdict(dict)
    itemCountMap = defaultdict(int)
    for key, cnt in pairCountMap.items():
        key1, key2 = key
        transitionCountMatrix[key1][key2] = cnt
        itemCountMap[key1] += cnt
        pairTotalCount += cnt
    transitionMatrix = defaultdict(dict)
    itemDistribution = defaultdict(dict)
    for key1, transitionMap in transitionCountMatrix.items():
        for key2, cnt in transitionMap.items():
            transitionMatrix[key1][key2] = transitionCountMatrix[key1][key2] / itemCountMap[key1]
    for itemid, cnt in itemCountMap.items():
        itemDistribution[itemid] = cnt / pairTotalCount
    return transitionMatrix, itemDistribution


def oneRandomWalk(transitionMatrix, itemDistribution, sampleLength):
    sample = []
    # pick the first element
    randomDouble = random.random()
    firstItem = ""
    accumulateProb = 0.0
    for item, prob in itemDistribution.items():
        accumulateProb += prob
        if accumulateProb >= randomDouble:
            firstItem = item
            break
    sample.append(firstItem)
    curElement = firstItem
    i = 1
    while i < sampleLength:
        if (curElement not in itemDistribution) or (curElement not in transitionMatrix):
            break
        probDistribution = transitionMatrix[curElement]
        randomDouble = random.random()
        accumulateProb = 0.0
        for item, prob in probDistribution.items():
            accumulateProb += prob
            if accumulateProb >= randomDouble:
                curElement = item
                break
        sample.append(curElement)
        i += 1
    return sample


def randomWalk(transitionMatrix, itemDistribution, sampleCount, sampleLength):
    samples = []
    for i in range(sampleCount):
        samples.append(oneRandomWalk(transitionMatrix, itemDistribution, sampleLength))
    return samples


def graphEmb(samples, spark, embLength, embOutputFilename, saveToRedis, redisKeyPrefix):
    transitionMatrix, itemDistribution = generateTransitionMatrix(samples)
    sampleCount = 20000
    sampleLength = 10
    newSamples = randomWalk(transitionMatrix, itemDistribution, sampleCount, sampleLength)
    rddSamples = spark.sparkContext.parallelize(newSamples)
    trainItem2vec(spark, rddSamples, embLength, embOutputFilename, saveToRedis, redisKeyPrefix)


def generateUserEmb(spark, rawSampleDataPath, model, embLength, embOutputPath, saveToRedis, redisKeyPrefix):
    ratingSamples = spark.read.format("csv").option("header", "true").load(rawSampleDataPath)
    Vectors_list = []
    for key, value in model.getVectors().items():
        Vectors_list.append((key, list(value)))
    fields = [
        StructField('game_id', StringType(), False),
        StructField('emb', ArrayType(FloatType()), False)
    ]
    schema = StructType(fields)
    Vectors_df = spark.createDataFrame(Vectors_list, schema=schema)
    ratingSamples = ratingSamples.join(Vectors_df, on='game_id', how='inner')
    result = ratingSamples.select('user_id', 'emb').rdd.map(lambda x: (x[0], x[1])) \
        .reduceByKey(lambda a, b: [a[i] + b[i] for i in range(len(a))]).collect()
    with open(embOutputPath, 'w') as f:
        for row in result:
            vectors = " ".join([str(emb) for emb in row[1]])
            f.write(row[0] + ":" + vectors + "\n")


if __name__ == '__main__':
    # Set python executable for Spark worker to ensure consistency
    import sys
    os.environ['PYSPARK_PYTHON'] = sys.executable
    os.environ['PYSPARK_DRIVER_PYTHON'] = sys.executable

    conf = SparkConf().setAppName('ctrModel').setMaster('local')
    spark = SparkSession.builder.config(conf=conf).getOrCreate()
    
    # Change to your own filepath
    # Use os.getcwd() to get the current working directory, assuming script is run from project root
    # Or you can hardcode the project root path here if you prefer
    project_root = os.getcwd()
    # Ensure we are using forward slashes for compatibility
    project_root = project_root.replace("\\", "/")
    
    # Define resource paths relative to project root
    # Note: Spark on Windows sometimes needs 'file:///' prefix for local files
    file_prefix = "file:///"
    
    rawSampleDataPath = file_prefix + project_root + "/src/main/resources/webroot/sampledata/ratings.csv"
    
    # Output paths
    modelDataDir = project_root + "/src/main/resources/webroot/modeldata"
    
    print(f"Reading data from: {rawSampleDataPath}")
    print(f"Outputting models to: {modelDataDir}")

    embLength = 10
    samples = processItemSequence(spark, rawSampleDataPath)
    
    model = trainItem2vec(spark, samples, embLength,
                          embOutputPath=modelDataDir + "/item2vecEmb.csv", saveToRedis=False,
                          redisKeyPrefix="i2vEmb")
    
    graphEmb(samples, spark, embLength, embOutputFilename=modelDataDir + "/itemGraphEmb.csv",
             saveToRedis=True, redisKeyPrefix="graphEmb")
    
    generateUserEmb(spark, rawSampleDataPath, model, embLength,
                    embOutputPath=modelDataDir + "/userEmb.csv", saveToRedis=False,
                    redisKeyPrefix="uEmb")
