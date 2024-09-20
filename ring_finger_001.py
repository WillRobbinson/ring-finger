import bluetooth
import json
import time
from smbus2 import SMBus
from mpu6050 import mpu6050

# Bluetooth setup
server_addr = "A0:7D:9C:7D:C7:C9"  # Replace with your Android device's Bluetooth MAC address
port = 1
uuid = "10dea90c-a7a0-4ad8-8ec8-b1dfb29c0659"

# MPU6050 setup
sensor = mpu6050(0x68)


def connect_bluetooth():

    print("Pre socket retrieval")
    sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    print("Post socket retrieval, socket: ")
    try:
        print("Pre service match")
        service_matches = bluetooth.find_service(uuid=uuid, address=server_addr)
        print("Post service match, len(service_matches)=["+str(len(service_matches))+"]")

        if len(service_matches) == 0:
            print("Couldn't find the specified service on the device")
            return None

        first_match = service_matches[0]
        port = first_match["port"]
        name = first_match["name"]
        host = first_match["host"]

        print(f"Connecting to {name} on {host}")
        sock.connect((host, port))
        print("Connected to the Android Bluetooth server")
        return sock
    except bluetooth.BluetoothError as e:
        print(f"Failed to connect: {e}")
        return None


def send_data(sock, data):
    print("In send data")
    try:
        json_data = json.dumps(data) +'\n'
        sock.send(json_data.encode())  # Add newline as a delimiter
        print(f"Sent data: {json_data.strip()}")
    except bluetooth.BluetoothError as e:
        print(f"Failed to send data: {e}")
        return False
    return True


def main():
    sock = connect_bluetooth()
    if not sock:
        print("Not sock, were done")
        return

    try:
        while True:
            print("Reading accelerometer data")
            # Read accelerometer and gyroscope data
            accel_data = sensor.get_accel_data()
            gyro_data = sensor.get_gyro_data()
            print("After polling both sensors")

            # Create data packet
            data = {
                "timestamp": time.time(),
                "accel_x": accel_data['x'],
                "accel_y": accel_data['y'],
                "accel_z": accel_data['z'],
                "gyro_x": gyro_data['x'],
                "gyro_y": gyro_data['y'],
                "gyro_z": gyro_data['z']
            }

            print("data:["+str(data)+"]")

            # Send data to Android server
            if not send_data(sock, data):
                break

            print("Taking short nap")
            time.sleep(0.1)  # Adjust the delay as needed

    except KeyboardInterrupt:
        print("Interrupted by user")
    finally:
        sock.close()
        print("Bluetooth connection closed")


if __name__ == "__main__":
    main()