import csv
import collections
import os

# Use relative path that works from project root
csv_path = os.path.join('src', 'main', 'resources', 'webroot', 'sampledata', 'games_filtered.csv')

genres_counter = collections.Counter()

try:
    with open(csv_path, mode='r', encoding='utf-8-sig') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            genres_str = row.get('Genres', '')
            if genres_str:
                genres = [g.strip() for g in genres_str.split(',')]
                for g in genres:
                    if g:
                        genres_counter[g] += 1

    print("Top Genres:")
    for genre, count in genres_counter.most_common(15):
        print(genre)

except Exception as e:
    print(f"Error: {e}")