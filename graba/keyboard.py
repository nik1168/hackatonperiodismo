import mraa #library used to have access to the GPIO of the microcontroller
import time #to enable time.sleep(1);
import pyupm_i2clcd as lcd

a0 = mraa.Aio(0); # read analog A0
a1 = mraa.Aio(1); # read analog A1
led = mraa.Gpio(13); # led located in digital pin 13
button = mraa.Gpio(5); # button placed in digital pin 5
lcdDisplay = lcd.Jhd1313m1(0, 0x3E, 0x62)  #LCD initialization


lcdDisplay.setColor(0,255,255)
lcdDisplay.setCursor(0, 0)
lcdDisplay.write("Rodrigo")
lcdDisplay.setCursor(1, 0)
lcdDisplay.write("isawesome")

while (1):
	if (a1.read() > 500):
		led.write(1)
	else:
		led.write(0)
	print(str(button.read()));
	print(str(a0.read()));
	print(str(a1.read()));
	time.sleep(1) #sleep for 1 second
