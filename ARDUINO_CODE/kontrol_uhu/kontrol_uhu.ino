//define LCD 16x2
#include <LiquidCrystal_I2C.h>
LiquidCrystal_I2C lcd (0x27, 16, 2);
String state;

//define Bluetooth Module
#include <SoftwareSerial.h>
SoftwareSerial bluetooth(2, 3); // pin RX | TX
unsigned long previousMillis = 0;
const long interval = 500;
static uint32_t tmp;
char data;

//define Driver Motor
#define enA 10
#define in1 9
#define in2 8
#define enB 5
#define in3 7
#define in4 6

//define DHT Sensor
#include <DHT.h>
#define DHTPIN 4
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);
float suhu;
float kelembapan;

#define relay 3
int motorSpeedA = 200;
int motorSpeedB = 0;

void setup() {
  //setup DHT11
  dht.begin();

  //setup LCD 16X2
  lcd.begin(16, 2);
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("HALLO");
  lcd.setCursor(0, 1);
  lcd.print("-_-");

  //  Serial.begin(9600);
  bluetooth.begin(9600);

  pinMode(relay, OUTPUT);
  digitalWrite(relay, HIGH);

  pinMode(enA, OUTPUT);
  pinMode(in1, OUTPUT);
  pinMode(in2, OUTPUT);
  digitalWrite(in1, LOW);
  digitalWrite(in2, LOW);
}

void loop() {
  if (bluetooth.available()) {
    data = bluetooth.read();

    if (data == '1') {
      motorSpeedA = 200;
      digitalWrite(relay, LOW);
      state = "ON";
    } else if (data == '0') {
      motorSpeedA = 0;
      digitalWrite(relay, HIGH);
      state = "OFF";
    } else if (data == '2') {//slow speed
      motorSpeedA = 150;
      state = "ON";
    } else if (data == '3') {//medium speed
      motorSpeedA = 200;
      state = "ON";
    } else if (data == '4') {//high speed
      motorSpeedA = 255;
      state = "ON";
    } else {
      lcd.clear();
      lcd.setCursor (0, 0);
      lcd.print("Invalid data receive");
    }
  }

  lcd.clear();
  kelembapan = dht.readHumidity();
  suhu = dht.readTemperature();

  printSensor(suhu, kelembapan);
  printSpeed(motorSpeedA, state);

  analogWrite(enA, motorSpeedA);
  digitalWrite(in1, HIGH);
  digitalWrite(in2, LOW);

  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;
    bluetooth.print(suhu);
  }

  delay(200);
}

void printSpeed(int pwm, String Mode) {
  lcd.setCursor (0, 1);
  lcd.print("MODE:");
  lcd.setCursor (5, 1);
  lcd.print(Mode);

  lcd.setCursor (9, 1);
  lcd.print("PWM:");
  lcd.setCursor (13, 1);
  lcd.print(pwm);

}

void printSensor(int s, int h) {
  lcd.setCursor(0, 0);
  lcd.print("S:");
  lcd.setCursor(2, 0);
  lcd.print(s);
  lcd.setCursor(4, 0);
  lcd.print("*C");

  lcd.setCursor(11, 0);
  lcd.print("S:");
  lcd.setCursor(13, 0);
  lcd.print(h);
  lcd.setCursor(15, 0);
  lcd.print("%");
}
