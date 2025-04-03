import psycopg
from sshtunnel import SSHTunnelForwarder
from pwd import *


def main():
    #AAAAAAAAAAAAa



try:
    with SSHTunnelForwarder(('starbug.cs.rit.edu', 22),
                            ssh_username=pwd.username,
                            ssh_password=pwd.password,
                            remote_bind_address=('127.0.0.1', 5432)) as server:
        server.start()
        print("SSH tunnel established")
        params = {
            'dbname': pwd.dbName,
            'user': pwd.username,
            'password': pwd.password,
            'host': 'localhost',
            'port': server.local_bind_port
        }


        conn = psycopg.connect(**params)
        curs = conn.cursor()
        print("Database connection established")

        main():

        conn.close()
except:
    print("Connection failed")