import csv
import os

csv_file_path = r'e:\Coding\Discipline Practice\project\GameRecSys\src\main\resources\webroot\sampledata\games_filtered.csv'
sql_file_path = r'e:\Coding\Discipline Practice\project\GameRecSys\migrate_data.sql'

def escape_sql_string(value):
    if value is None:
        return 'NULL'
    return "'" + value.replace("'", "''").replace("\\", "\\\\") + "'"

def parse_bool(value):
    if value.lower() == 'true':
        return '1'
    if value.lower() == 'false':
        return '0'
    return 'NULL'

def parse_number(value):
    if not value:
        return 'NULL'
    return value

with open(csv_file_path, 'r', encoding='utf-8-sig') as csvfile, open(sql_file_path, 'w', encoding='utf-8') as sqlfile:
    reader = csv.DictReader(csvfile)
    
    sqlfile.write("USE gamerecsys;\n")
    sqlfile.write("SET NAMES utf8mb4;\n")
    sqlfile.write("TRUNCATE TABLE game_data;\n")
    
    for row in reader:
        # Map CSV columns to Table columns
        # Table columns: AppID, Name, Release_date, Estimated_owners, Peak_CCU, Required_age, Price, Discount, DLC_count, About_the_game, Supported_languages, Full_audio_languages, Reviews, Header_image, Website, Support_url, Support_email, Windows, Mac, Linux, Metacritic_score, Metacritic_url, User_score, Positive, Negative, Score_rank, Achievements, Recommendations, Notes, Average_playtime_forever, Average_playtime_two_weeks, Median_playtime_forever, Median_playtime_two_weeks, Developers, Publishers, Categories, Genres, Tags, Screenshots, Movies
        
        values = []
        values.append(parse_number(row['AppID']))
        values.append(escape_sql_string(row['Name']))
        values.append(escape_sql_string(row['Release date']))
        values.append(escape_sql_string(row['Estimated owners']))
        values.append(parse_number(row['Peak CCU']))
        values.append(parse_number(row['Required age']))
        values.append(parse_number(row['Price']))
        values.append(parse_number(row['Discount']))
        values.append(parse_number(row['DLC count']))
        values.append(escape_sql_string(row['About the game']))
        values.append(escape_sql_string(row['Supported languages']))
        values.append(escape_sql_string(row['Full audio languages']))
        values.append(escape_sql_string(row['Reviews']))
        values.append(escape_sql_string(row['Header image']))
        values.append(escape_sql_string(row['Website']))
        values.append(escape_sql_string(row['Support url']))
        values.append(escape_sql_string(row['Support email']))
        values.append(parse_bool(row['Windows']))
        values.append(parse_bool(row['Mac']))
        values.append(parse_bool(row['Linux']))
        values.append(parse_number(row['Metacritic score']))
        values.append(escape_sql_string(row['Metacritic url']))
        values.append(parse_number(row['User score']))
        values.append(parse_number(row['Positive']))
        values.append(parse_number(row['Negative']))
        values.append(escape_sql_string(row['Score rank']))
        values.append(parse_number(row['Achievements']))
        values.append(parse_number(row['Recommendations']))
        values.append(escape_sql_string(row['Notes']))
        values.append(parse_number(row['Average playtime forever']))
        values.append(parse_number(row['Average playtime two weeks']))
        values.append(parse_number(row['Median playtime forever']))
        values.append(parse_number(row['Median playtime two weeks']))
        values.append(escape_sql_string(row['Developers']))
        values.append(escape_sql_string(row['Publishers']))
        values.append(escape_sql_string(row['Categories']))
        values.append(escape_sql_string(row['Genres']))
        values.append(escape_sql_string(row['Tags']))
        values.append(escape_sql_string(row['Screenshots']))
        values.append(escape_sql_string(row['Movies']))
        
        sql = f"INSERT INTO game_data VALUES ({', '.join(values)});\n"
        sqlfile.write(sql)

print(f"Generated SQL file at {sql_file_path}")
