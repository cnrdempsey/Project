#include <SoftwareSerial.h>

float vPow = 5.98;
float r1 = 1000;
float r2 = 460;
float r3 = 100;
float r4 = 330;
int delayMs = 100; 
int ledPin = 13; // use the built in LED on pin 13 of the Uno
String data = "";
int dataRecievedFlag = 0;        // make sure that you return the state only once
SoftwareSerial mySerial(10, 11); // RX, TX

void setup() {
  // sets the pins as outputs:
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Serial.begin(9600); // Default connection rate for my BT module
  mySerial.begin(57600); // Default connection rate for my BT module
  while(!Serial){
    Serial.println("--------------------");
    Serial.println("MULTIMETER");
    Serial.println("--------------------");
  }
  delay(2000);
}

float voltmeter(int n){
  float valuesArr[n];
  
  for(int i =0; i < n; i++){
    float v = (analogRead(0) * vPow) / 1024.0;
    float v2 = v / (r2 / (r1 + r2));
    valuesArr[i] = v2;
    delay(delayMs);
  }

  float voltAver = 0;
  for(int i =0; i < n; i++){
    voltAver += valuesArr[i];
  }
  
  Serial.println("Voltage = " + (String)(voltAver/n));
  return voltAver/10;
}


float ammeter(int n){
  float valuesArrFirst[n];
  float valuesArrSecond[n];
  
  for(int i =0; i < n; i++){
    float vFirst = (analogRead(1) * vPow) / 1024.0;
    float vSecond = (analogRead(2) * vPow) / 1024.0;        
    valuesArrFirst[i] = vFirst;
    valuesArrSecond[i] = vSecond;
    delay(delayMs);
  }

  float currAver = 0;
  for(int i =0; i < n; i++){
    currAver += abs((valuesArrSecond[i] - valuesArrFirst[i])  / r3);
  }
  
  Serial.println("Current = " + (String)(currAver/n));
  return currAver/n;
}

float ohmmeter(int n){
  float valuesArr[n];
  
  for(int i =0; i < n; i++){
    float raw = analogRead(3); 
    float vout = (raw * vPow)/ 1024;
    float buffer = (vPow/vout) - 1 ;
    float res = r4 * 1/buffer;     
    valuesArr[i] = res;
    delay(delayMs);
  }

  float resAver = 0;
  for(int i =0; i < n; i++){
    resAver += valuesArr[i];
  }
  
  Serial.println("Resistance = " + (String)(resAver/n));
  return resAver/n;
}

void readData(){
  if (mySerial.available()) {
    Serial.print("State recieved: ");
    while(mySerial.available()) { // While there is more to be read, keep reading.
      data += (char)mySerial.read();
    }
    dataRecievedFlag=1;
  }
}

void ledSwitch(){  
  Serial.write("Flag set");
  // if the state is 0 the led will turn off
  if (data == "0") {
    digitalWrite(ledPin, LOW);
    Serial.println("LED: off");
  }
  // if the state is 1 the led will turn on
  else if (data == "1") {
    digitalWrite(ledPin, HIGH);
    Serial.println("LED: on");    
  }  
  dataRecievedFlag=0;
  data="";    
}

String createResultString(float result){
  String tempStr = String(result, 2);
  String resultStr = "";
  if(tempStr.length() < 8){
      for(int i = 0; i < (8 - tempStr.length()); i ++){
        resultStr += "0";
      }
  }
  resultStr += tempStr;
  return resultStr;
}

String createFullMessage(float volt, float current, float res){
    String mess = "#" + createResultString(volt) + "~" + createResultString(current) + "~" + createResultString(res); 
    return mess;  
}

String createPartialMessage(float result){
    String mess = "#" + createResultString(result); 
    return mess;  
}


void loop() {
  float voltmeterResult = voltmeter(10);
  //float ammeterResult = ammeter(10);
 // float ohmmeterResult = ohmmeter(10);

  readData();

 // String data = createMessage(voltmeterResult,ammeterResult,ohmmeterResult); 
String data = createPartialMessage(voltmeterResult);
//  Serial.println(data.substring(1, 9));
//  Serial.println(data.substring(10, 18));
//  Serial.println(data.substring(19, 27));
  
  int str_len = data.length() + 1; 
  char char_array[str_len];
  data.toCharArray(char_array, str_len);
  Serial.println(char_array);
  mySerial.write(char_array);
      
  if(dataRecievedFlag == 1 ){
    ledSwitch();
  }
    
}
 
