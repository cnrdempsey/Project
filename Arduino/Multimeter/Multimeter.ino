#include <SoftwareSerial.h>

float vPow = 5.98;
float r1 = 1000;
float r2 = 460;
float r3 = 100;
float r4 = 330;
int delayMs = 150; //delay 
int ledPin = 13; // use the built in LED on pin 13 of the Uno
String data = ""; //data received
int dataRecievedFlag = 0;    // boolean to showing when a message is received
int startMeasurement = 0;   // boolean to start measurement
float dataToSend; //message to send 
SoftwareSerial mySerial(10, 11);

void setup() {
  // sets the pins as outputs:
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Serial.begin(9600); 
  mySerial.begin(57600); // Connection rate for my BT module
  while(!Serial){
    Serial.println("--------------------");
    Serial.println("MULTIMETER");
    Serial.println("--------------------");
  }
  delay(2000);
}

//function to measure voltage
void voltmeter(int n){
  Serial.println("Voltmeter Started ... ");
   float voltAver = 0;
  for(int i =0; i < n; i++){
    float v = (analogRead(0) * vPow) / 1024.0;
    float v2 = v / (r2 / (r1 + r2));
    voltAver += v2;
    delay(delayMs);
  }
  
  Serial.println("Voltage = " + (String)(voltAver/n));
  dataToSend = voltAver/n;
}

//function to measure current
void ammeter(int n){
  Serial.println("Ammeter Started ... ");
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
  dataToSend =  currAver/n;
}

//function to measure resistance
void ohmmeter(int n){
  Serial.println("Ohmmeter Started ... ");
  float resAver = 0;
  for(int i =0; i < n; i++){
    float raw = analogRead(3); 
    float vout = (raw * vPow)/ 1024;
    float buffer = (vPow/vout) - 1 ;
    float res = r4 * 1/buffer;
    resAver += res;     
    delay(delayMs);
  }
  
  Serial.println("Resistance = " + (String)(resAver/n));
  dataToSend =  resAver/n;
}

//function to read data over the Bluetooth connection
void readData(){
  Serial.println("Reading ...");
  if (mySerial.available()) {
    Serial.println("State recieved");
    // While there is more to be read, keep reading.
    while(mySerial.available()) { 
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

//function that handles the measurement mode of the multimeter based on data sent over BT
void switchMeasurementMode(){ 
 
  Serial.write("Flag set");
  if (data == "1") {
    Serial.write("Voltage Measurement");
    voltmeter(10);
  }
  else if (data == "2") {
    Serial.write("Current Measurement");
    ammeter(10);   
  }
  else if (data == "3") {
    Serial.write("Resistance Measurement");
    ohmmeter(10);   
  }else {
      dataRecievedFlag = 0;
  }  
     
}

//function that creates a message packet of 8 bytes
String createResultString(float result){
  String tempStr = String(result, 2);
  String resultStr = "";
  //if result string is not 8 long, leading zereos are added
  if(tempStr.length() < 8){
      for(int i = 0; i < (8 - tempStr.length()); i ++){
        resultStr += "0";
      }
       resultStr += tempStr;
  }else if(tempStr.length() > 9){ //if the result string is > 8 an error is generated
     resultStr = "Error...";
  }
  return resultStr;
}

void loop() {
  //check for a message over the BT connection
  readData();
  //if a message is recieved, this block is ran
  if(dataRecievedFlag == 1 ){
    //calls a measurement function based on the message
    switchMeasurementMode();
    //creates a message packet to be sent
    String data = "#" + createResultString(dataToSend); 
    int str_len = data.length() + 1; 
    char char_array[str_len];
    //converts the result string to char array to be sent
    data.toCharArray(char_array, str_len);
    Serial.println(char_array);
    mySerial.write(char_array);
    
  }
    
}
 
