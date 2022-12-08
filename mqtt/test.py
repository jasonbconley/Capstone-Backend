import paho.mqtt.client as mqtt #import the client1
import time

def on_message(client, userdata, message):
    print("message received " ,str(message.payload.decode("utf-8")))
    print("message topic=",message.topic)
    print("message qos=",message.qos)
    print("message retain flag=",message.retain)

broker_address="localhost"
mqttUser = 'capstone'
mqttPassword = 'track'

print("creating new instance")
client = mqtt.Client("P1") #create new instance
client.on_message=on_message #attach function to callback
client.username_pw_set(username=mqttUser, password=mqttPassword)

print("connecting to broker")
client.connect(broker_address) #connect to broker
client.loop_start() #start the loop

print("subscribing to test")
client.subscribe("test")
time.sleep(4) # wait
client.loop_forever() #stop the loop
