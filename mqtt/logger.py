import paho.mqtt.client as mqtt
import mariadb
import sys
import json5
import time

#User variable for database name
dbName = "capstonedb"

# it is expected that this Database will already contain one table called sensors.  Create that table inside the Database with this command:
# CREATE TABLE sensors(device_id char(23) NOT NULL, transmission_count INT NOT NULL, battery_level FLOAT NOT NULL, type INT NOT NULL, node_id INT NOT NULL, rssi INT NOT NULL, last_heard TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);

# User variables for MQTT Broker connection
mqttBroker = "laserlads.xyz"
mqttBrokerPort = 1883
mqttUser = 'capstone'
mqttPassword = 'track'
mqttTopic = 'test'
#mqttCert = "/home/capstone/certs/key.crt"

mariaHost = "localhost"
mariaUser = "capstone"
mariaPassword = "track"

lot = "MSBR5"

# This callback function fires when the MQTT Broker conneciton is established.  At this point a connection to MySQL server will be attempted.
def on_connect(client, userdata, flags, rc):
    print("Creating maria connection")
    try:
        global conn
        conn = mariadb.connect(
            user=mariaUser,
            password=mariaPassword,
            host="localhost",
            port=3306,
            database=dbName,
            autocommit=True
        )
    except mariadb.Error as e:
        print(f"Error connecting to MariaDB Platform: {e}")
        sys.exit(1)

    print("Maria connection created")

    global cur 
    cur = conn.cursor()

def on_log(client, userdata, level, buf):
    print("buffer", buf)

def print_json(payload):
    print(payload['Dur'])

# This function updates the sensor's information in the sensor index table
def sensor_update(payload):
    duration = int(payload['Dur'])
    direction = payload['Dir']

    query = "SELECT * FROM lots WHERE name=\"{}\"".format(lot)

    try:
        cur.execute(query)        
    except mariadb.Error as e:
        print(f"Error: {e}")

    result = cur.fetchone()
    taken = result[2]
    total = result[1]

    new_taken = taken
    if duration < 9 and duration > 5:
        if direction == "IN":
            new_taken += 1
        elif direction == "OUT":
            new_taken -= 1
        else:
            return "Data quality issue"

    # Only commit new count if value is above -1 and below lot threshold
    if (new_taken > -1 and new_taken <= total):
        query = "UPDATE lots SET taken={} WHERE name = \"{}\"".format(new_taken, lot)

        try:
            cur.execute(query)
        except mariadb.Error as e:
            print(f"Error: {e}")

        conn.commit()
        return "Data committed"
    else:
        return "Data quality issue"

#additional callback for updating lot table, if positive, then we update lot number by 1, if negative, -1
def update_lot(db, direction):
    cursor = db.cursor()
    dirBool = True if direction > 0 else False
    updateRequest = "UPDATE capstone.lots SET taken = taken + 1 WHERE name = 'MSBR5'" if dirBool is True else "UPDATE capstone.lots SET taken = taken - 1 WHERE name = 'MSBR5'"
    cursor.execute(updateRequest)
    db.commit()

# The callback for when a PUBLISH message is received from the MQTT Broker.
def on_message(client, userdata, msg):
    print("Transmission received")
    payload = json5.loads(msg.payload.decode("utf-8"))
    sensor_update(payload)
    print('data logged')

# Connect the MQTT Client
client = mqtt.Client()
client.log = on_log
client.on_connect = on_connect
client.on_message = on_message
client.username_pw_set(username=mqttUser, password=mqttPassword)
#client.tls_set(mqttCert)

try:
    client.connect(mqttBroker, mqttBrokerPort, 60)
    client.subscribe("test", qos=0)
except:
    sys.exit("Connection to MQTT Broker failed")
# Stay connected to the MQTT Broker indefinitely
client.loop_forever()

