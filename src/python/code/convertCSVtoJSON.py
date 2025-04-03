# import psycopg
from sshtunnel import SSHTunnelForwarder
import pwd
import sqlite3
  
# Connecting to sqlite 
# connection object 
connection_obj = sqlite3.connect('src/python/dataset/track_metadata.db') 
  
# cursor object 
cursor_obj = connection_obj.cursor() 
  
# to select all column we will use 
statement = '''SELECT title, duration, artist_name FROM songs LIMIT 10'''

  
cursor_obj.execute(statement) 
  
print("All the data") 
output = cursor_obj.fetchall() 
for row in output: 
  print(row) 
  
connection_obj.commit() 
  
connection_obj.close()