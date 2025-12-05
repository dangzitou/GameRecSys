# GameRecSys
GameRecSys是一个游戏推荐系统（原为电影推荐系统），名字GameRecSys（Fork from [SparrowRecSys](https://github.com/wzhe06/SparrowRecSys)），取自“麻雀虽小，五脏俱全”之意。项目是一个基于maven的混合语言项目，同时包含了TensorFlow，Spark，Jetty Server等推荐系统的不同模块。

## 环境要求
* Java 8+
* Maven 3+
* MySQL 8.0+
* (Optional) IntelliJ IDEA or VS Code

## 快速开始

### 1. 数据库配置
1. 确保 MySQL 服务已启动。
2. 创建数据库并初始化表结构：
   在 MySQL 中执行 `src/main/resources/db_scripts/init_db.sql` 脚本。
   ```sql
   -- 示例命令 (请根据实际路径调整)
   source src/main/resources/db_scripts/init_db.sql;
   ```
3. 导入初始数据：
   由于数据文件 `migrate_data.sql` 较大，建议直接在终端（命令行）运行以下命令导入，而不是复制粘贴内容。
   
   在项目根目录下cmd执行：
   ```bash
   mysql -u root -p gamerecsys < src/main/resources/db_scripts/migrate_data.sql
   ```
   *(输入命令后回车，然后输入你的 MySQL 密码)*
   
   如果是在vscode终端（默认使用Powershell），执行：
   ```bash
   cmd /c "mysql -u root -p gamerecsys < src/main/resources/db_scripts/migrate_data.sql"
   ```
   或者在 MySQL 交互式命令行中执行：
   ```sql
   use gamerecsys;
   source src/main/resources/db_scripts/migrate_data.sql;
   ```
4. 修改数据库连接配置：
   打开 `src/main/resources/mybatis-config.xml`，找到 `<dataSource>` 标签，修改 `username` 和 `password` 为你的 MySQL 用户名和密码。
   ```xml
   <property name="username" value="root"/>
   <property name="password" value="your_password"/>
   ```

### 2. 启动项目

#### 方式一：使用 IntelliJ IDEA (推荐)
1. 用 IntelliJ IDEA 打开项目。
2. 等待 Maven 依赖下载完成。
3. 找到 `src/main/java/com/sparrowrecsys/online/RecSysServer.java` 文件。
4. 右键点击 `RecSysServer` 类，选择 `Run 'RecSysServer.main()'`.

#### 方式二：使用命令行
1. 在项目根目录下编译项目：
   ```bash
   mvn clean compile
   ```
2. 启动服务：
   ```bash
   mvn exec:java -Dexec.mainClass="com.sparrowrecsys.online.RecSysServer"
   ```
   或者打包运行：
   ```bash
   mvn package
   java -jar target/SparrowRecSys-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

### 3. 访问系统
打开浏览器访问：[http://localhost:6010/](http://localhost:6010/)

## 项目数据
项目数据为游戏数据集（`games_filtered.csv`），来源于Steam等平台的游戏数据。

## 项目进度与计划

### 已实现功能 (Implemented)
- [x] **前端页面开发**: 完成了首页、游戏详情页、用户页等基础页面 (Webroot)。
- [x] **基础推荐服务**:
  - 游戏详情接口 (`/getgame`)
  - 用户详情接口 (`/getuser`)
  - 相似游戏推荐 (`/getsimilargame`)
  - 猜你喜欢 (`/getrecommendation`)
  - 个性化推荐 (`/getrecforyou`)
- [x] **搜索与交互**:
  - 游戏搜索接口 (`/search`)
  - 用户评分接口 (`/rating`)
  - 用户认证接口 (`/auth/*`)
- [x] **基础设施**:
  - 数据库集成 (MySQL + MyBatis)
  - 嵌入式 Jetty 服务器
  - 基础数据加载 (DataManager)

### 待实现功能 (Todo List)
- [ ] **实时推荐更新 (Real-time Updates)**
  - [ ] 在 `RatingController` 中添加逻辑，用户评分后实时更新内存中的用户画像。
  - [ ] 实现基于内存的简易协同过滤或规则调整，即时反馈用户行为。
- [ ] **离线模型训练管道 (Offline Training Pipeline)**
  - [ ] 编写 Python/Spark 脚本，定期从 MySQL 导出评分数据。
  - [ ] 实现模型训练脚本 (Word2Vec, NeuralCF 等)，生成新的 Embedding 向量。
- [ ] **模型服务化 (Model Serving)**
  - [ ] 实现 Embedding 数据的热加载机制 (无需重启服务器)。
  - [ ] (可选) 搭建 TensorFlow Serving 服务，通过 gRPC 调用复杂模型。
- [ ] **系统运维与监控**
  - [ ] 添加管理接口，用于手动触发数据重载或模型更新。
  - [ ] 完善日志记录，确保用户行为数据可追溯。

---

本项目改造自[SparrowRecSys](https://github.com/wzhe06/SparrowRecSys)。

## SparrowRecSys技术架构
SparrowRecSys技术架构遵循经典的工业级深度学习推荐系统架构，包括了离线数据处理、模型训练、近线的流处理、线上模型服务、前端推荐结果显示等多个模块。以下是SparrowRecSys的架构图：
![alt text](https://github.com/wzhe06/SparrowRecSys/raw/master/docs/sparrowrecsysarch.png)

## SparrowRecSys实现的深度学习模型
* Word2vec (Item2vec)
* DeepWalk (Random Walk based Graph Embedding)
* Embedding MLP
* Wide&Deep
* Nerual CF
* Two Towers
* DeepFM
* DIN(Deep Interest Network)

## 相关论文
* [[FFM] Field-aware Factorization Machines for CTR Prediction (Criteo 2016)](https://github.com/wzhe06/Ad-papers/blob/master/Classic%20CTR%20Prediction/%5BFFM%5D%20Field-aware%20Factorization%20Machines%20for%20CTR%20Prediction%20%28Criteo%202016%29.pdf) <br />
* [[GBDT+LR] Practical Lessons from Predicting Clicks on Ads at Facebook (Facebook 2014)](https://github.com/wzhe06/Ad-papers/blob/master/Classic%20CTR%20Prediction/%5BGBDT%2BLR%5D%20Practical%20Lessons%20from%20Predicting%20Clicks%20on%20Ads%20at%20Facebook%20%28Facebook%202014%29.pdf) <br />
* [[PS-PLM] Learning Piece-wise Linear Models from Large Scale Data for Ad Click Prediction (Alibaba 2017)](https://github.com/wzhe06/Ad-papers/blob/master/Classic%20CTR%20Prediction/%5BPS-PLM%5D%20Learning%20Piece-wise%20Linear%20Models%20from%20Large%20Scale%20Data%20for%20Ad%20Click%20Prediction%20%28Alibaba%202017%29.pdf) <br />
* [[FM] Fast Context-aware Recommendations with Factorization Machines (UKON 2011)](https://github.com/wzhe06/Ad-papers/blob/master/Classic%20CTR%20Prediction/%5BFM%5D%20Fast%20Context-aware%20Recommendations%20with%20Factorization%20Machines%20%28UKON%202011%29.pdf) <br />
* [[DCN] Deep & Cross Network for Ad Click Predictions (Stanford 2017)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BDCN%5D%20Deep%20%26%20Cross%20Network%20for%20Ad%20Click%20Predictions%20%28Stanford%202017%29.pdf) <br />
* [[Deep Crossing] Deep Crossing - Web-Scale Modeling without Manually Crafted Combinatorial Features (Microsoft 2016)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BDeep%20Crossing%5D%20Deep%20Crossing%20-%20Web-Scale%20Modeling%20without%20Manually%20Crafted%20Combinatorial%20Features%20%28Microsoft%202016%29.pdf) <br />
* [[PNN] Product-based Neural Networks for User Response Prediction (SJTU 2016)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BPNN%5D%20Product-based%20Neural%20Networks%20for%20User%20Response%20Prediction%20%28SJTU%202016%29.pdf) <br />
* [[DIN] Deep Interest Network for Click-Through Rate Prediction (Alibaba 2018)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BDIN%5D%20Deep%20Interest%20Network%20for%20Click-Through%20Rate%20Prediction%20%28Alibaba%202018%29.pdf) <br />
* [[ESMM] Entire Space Multi-Task Model - An Effective Approach for Estimating Post-Click Conversion Rate (Alibaba 2018)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BESMM%5D%20Entire%20Space%20Multi-Task%20Model%20-%20An%20Effective%20Approach%20for%20Estimating%20Post-Click%20Conversion%20Rate%20%28Alibaba%202018%29.pdf) <br />
* [[Wide & Deep] Wide & Deep Learning for Recommender Systems (Google 2016)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BWide%20%26%20Deep%5D%20Wide%20%26%20Deep%20Learning%20for%20Recommender%20Systems%20%28Google%202016%29.pdf) <br />
* [[xDeepFM] xDeepFM - Combining Explicit and Implicit Feature Interactions for Recommender Systems (USTC 2018)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BxDeepFM%5D%20xDeepFM%20-%20Combining%20Explicit%20and%20Implicit%20Feature%20Interactions%20for%20Recommender%20Systems%20%28USTC%202018%29.pdf) <br />
* [[Image CTR] Image Matters - Visually modeling user behaviors using Advanced Model Server (Alibaba 2018)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BImage%20CTR%5D%20Image%20Matters%20-%20Visually%20modeling%20user%20behaviors%20using%20Advanced%20Model%20Server%20%28Alibaba%202018%29.pdf) <br />
* [[AFM] Attentional Factorization Machines - Learning the Weight of Feature Interactions via Attention Networks (ZJU 2017)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BAFM%5D%20Attentional%20Factorization%20Machines%20-%20Learning%20the%20Weight%20of%20Feature%20Interactions%20via%20Attention%20Networks%20%28ZJU%202017%29.pdf) <br />
* [[DIEN] Deep Interest Evolution Network for Click-Through Rate Prediction (Alibaba 2019)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BDIEN%5D%20Deep%20Interest%20Evolution%20Network%20for%20Click-Through%20Rate%20Prediction%20%28Alibaba%202019%29.pdf) <br />
* [[DSSM] Learning Deep Structured Semantic Models for Web Search using Clickthrough Data (UIUC 2013)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BDSSM%5D%20Learning%20Deep%20Structured%20Semantic%20Models%20for%20Web%20Search%20using%20Clickthrough%20Data%20%28UIUC%202013%29.pdf) <br />
* [[FNN] Deep Learning over Multi-field Categorical Data (UCL 2016)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BFNN%5D%20Deep%20Learning%20over%20Multi-field%20Categorical%20Data%20%28UCL%202016%29.pdf) <br />
* [[DeepFM] A Factorization-Machine based Neural Network for CTR Prediction (HIT-Huawei 2017)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BDeepFM%5D%20A%20Factorization-Machine%20based%20Neural%20Network%20for%20CTR%20Prediction%20%28HIT-Huawei%202017%29.pdf) <br />
* [[NFM] Neural Factorization Machines for Sparse Predictive Analytics (NUS 2017)](https://github.com/wzhe06/Ad-papers/blob/master/Deep%20Learning%20CTR%20Prediction/%5BNFM%5D%20Neural%20Factorization%20Machines%20for%20Sparse%20Predictive%20Analytics%20%28NUS%202017%29.pdf) <br />

## 其他相关资源
* [Papers on Computational Advertising](https://github.com/wzhe06/Ad-papers) <br />
* [Papers on Recommender System](https://github.com/wzhe06/Ad-papers) <br />
* [CTR Model Based on Spark](https://github.com/wzhe06/SparkCTR) <br />
