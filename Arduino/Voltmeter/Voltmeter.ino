float vPow = 6.1;
 float r1 = 1000;
 float r2 = 460;
 
 void setup() {
   Serial.begin(9600);
  while(!Serial){
   Serial.println("--------------------");
   Serial.println("DC VOLTMETER");
   Serial.print("Maximum Voltage: ");
   Serial.print((int)(vPow / (r2 / (r1 + r2))));
   Serial.println("V");
   Serial.println("--------------------");
  }
   delay(2000);
 }
 
 void loop() {
   float valuesArr[10];
  
   for(int i =0; i < 10; i++){
       float v = (analogRead(0) * vPow) / 1024.0;
       float v2 = v / (r2 / (r1 + r2));
       valuesArr[i] = v2;
       delay(500);
   }

   float voltAver = 0;
   for(int i =0; i < 10; i++){
       voltAver += valuesArr[i];
       Serial.print(" ");
       Serial.print(valuesArr[i]);
   }
   
   Serial.print(" Voltage = ");
   Serial.println(voltAver/10);
 }

