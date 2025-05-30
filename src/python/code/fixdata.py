import psycopg
from sshtunnel import SSHTunnelForwarder
from pwd import *


def main(conn):
    a = "SELECT * FROM ALBUM where album_id = 1"
    conn.execute(a)
    why = conn.fetchone()
    print(why)



try:
    with SSHTunnelForwarder(('starbug.cs.rit.edu', 22),
                            ssh_username=username,
                            ssh_password=password,
                            remote_bind_address=('127.0.0.1', 5432)) as server:
        server.start()
        print("SSH tunnel established")
        params = {
            'dbname': dbName,
            'user': username,
            'password': password,
            'host': 'localhost',
            'port': server.local_bind_port
        }


        conn = psycopg.connect(**params)
        curs = conn.cursor()
        print("Database connection established")

        main(conn)

        conn.close()
except:
    print("Connection failed")