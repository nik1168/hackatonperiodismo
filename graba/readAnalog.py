import mraa #library used to have access to the GPIO of the microcontroller
import time #to enable time.sleep(1);

a0 = mraa.Aio(0); # read analog A0
a1 = mraa.Aio(1); # read analog A1
led = mraa.Gpio(13); # led located in digital pin 13
button = mraa.Gpio(5); # button placed in digital pin 5

while (1):
	if (a1.read() > 500):
		led.write(1)
	else:
		led.write(0)
	print(str(button.read()));
	print(str(a0.read()));
	print(str(a1.read()));
	time.sleep(1) #sleep for 1 second
