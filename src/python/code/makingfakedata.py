import numpy as np
import csv
from datetime import datetime, timedelta
import random

num_rows = 50000
mean_song_id = random.randint(500, 4500) 
std_song_id = 350
min_user_id = 1
max_user_id = 5000
start_date = datetime(2010, 1, 1)
end_date = datetime(2025, 12, 31)

data = []
for _ in range(num_rows):
    list_user_id = random.randint(min_user_id, max_user_id)
    list_song_id = abs(int(np.random.normal(mean_song_id, std_song_id)))
    random_date = start_date + timedelta(seconds=random.randint(0, int((end_date - start_date).total_seconds())))
    date_time_listened = random_date.strftime('%Y-%m-%d %H:%M:%S')
    data.append([list_user_id, list_song_id, date_time_listened])


print("\n\n\n"+ str(mean_song_id) + "\n\n\n")

output_file = 'out.csv'
with open(output_file, mode='w', newline='') as file:
    writer = csv.writer(file)
    writer.writerow(['list_user_id', 'list_song_id', 'date_time_listened'])  # Header
    writer.writerows(data)