/*
Event Type                      Description
1                               Car arrives at NS
2                               Car arrives at EW
3                               Car enters service bay (intersection) at NS
4                               Car enters service bay (intersection) at EW
5                               EW light turns green
6                               EW light turns red
7                               Car departs intersection at NS
8                               Car departs intersection at EW
9                               Simulation shut down
 */

 import java.util.ArrayList;
 public class StopLight {
     public static void main(String[] args) {
         double bigTime = 0.0; //simulation main clock in seconds
         double eventTime; //event time
         double deltaTime;
 
         GenericManager<Event> EventQueue = new GenericManager<Event>();
         GenericManager<Car> NSQueue = new GenericManager<Car>();
         GenericManager<Car> EWQueue = new GenericManager<Car>();
 
         int numInQueueNS;
         int numInQueueEW;
         int numInEvent; //number of events in event queue
         double lightTime = 120;
 
         double EWThruLine = 0, NSThruLine = 0;
         double EWThruSystem = 0, NSThruSystem = 0;
         double EWTimeInLine =0, NSTimeInLine =0;
         double totalTimeInServer = 0;
         double ttil, ttis;
         boolean busyNS = false;
         boolean busyEW = false;
         boolean green = true;
         Car servedNS = new Car(0, 0);
         Car servedEW = new Car(0, 0);
 
         //create service time
         double deltaTimeServe = 0;
 
         //create arrive times
         double deltaTimeArrive = 0;
 
         //create light change times
         double deltaTimeLight = 0;
 
         Car newCar = new Car(0, 0);//this is a work customer obj for those entering queue
         Car workCar = new Car(0, 0);//this is a work customer object for those coming from queue
 
         //create last event of simulation
         Event workEvent = new Event(9, 3600000.00);
         //add the event in queue
         numInEvent = EventQueue.addInOrder(workEvent);
         //add the arrival for the first customers
         deltaTimeArrive = TimeToArriveOrServe(0.3);//cars arrive at a rate of 1/3 sec
         //the event time is current time plus delta time
         eventTime = bigTime+deltaTimeArrive;
         deltaTimeArrive = TimeToArriveOrServe(0.3);
         double eventTime1 = bigTime + deltaTimeArrive + eventTime;
         workEvent = new Event(2, eventTime1);
         numInEvent = EventQueue.addInOrder(workEvent);
         System.out.println("The first car arrives at " + eventTime);
         workEvent = new Event(1, eventTime);
         numInEvent = EventQueue.addInOrder(workEvent);
         System.out.println("The second car arrives at " + eventTime1);
 
         //store event in queue
         numInEvent = EventQueue.addInOrder(workEvent);
 
 
         //create a light turn event
         workEvent = new Event(5, 120.0);
         numInEvent = EventQueue.addInOrder(workEvent);
 
         //begin processing events
         //get first event off of event queue
         workEvent = EventQueue.getValue(0);
         while (workEvent.getEventType() != 9) {
             //valid event requires time update
             //PrintEventQueue(EventQueue);
             deltaTime = workEvent.getTime() - bigTime;
             //update everyone with this deltatime
             ttil = UpdateCustomer(NSQueue, deltaTime);
             NSTimeInLine += ttil;
             ttil = UpdateCustomer(EWQueue, deltaTime);
             EWTimeInLine += ttil;
             ttis = UpdateServers(servedNS, servedEW, busyNS, busyEW, deltaTime);
             totalTimeInServer += ttis;
 
             bigTime = workEvent.getTime();
             //System.out.println("Bigtime is " +bigTime);
 
             if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                 System.out.println("*****THE TIME IS NOW " + bigTime + "*****");
                 System.out.println();
             }
 
             //get number in customer queue
             numInQueueNS = NSQueue.getCount();
             numInQueueEW = EWQueue.getCount();
 
             switch (workEvent.getEventType()) {
                 case 1://car arrives at NS
                     if ((busyNS == false) && (numInQueueNS <= 0)) {
                         //intersection is empty and no cars in line at NS
                         if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                             System.out.println("Car enters intersection from NS");
                             System.out.println();
                         }
                         newCar = new Car(1, generateTurnNS());
                         //set arrival time for car
                         newCar.setArrive(bigTime);
                         busyNS = true;
                         servedNS = newCar;
                         //generate the finished server event for this car
                         if(servedNS.getTurn() == 0){
                             // car turns north
                             deltaTimeServe = TimeToArriveOrServe(0.25);//1/4 seconds when turning north
                         }
                         else if(servedNS.getTurn() == 2){
                             //car turns south
                             deltaTimeServe = TimeToArriveOrServe(0.125);// 1/8 seconds when turning west
                         }
                         eventTime = deltaTimeServe+bigTime;
                         workEvent = new Event (7, eventTime);
                         //put this event in the event queue
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }//customer is in intersection
 
                     else if(busyNS == true){
                         //intersection is busy, put car in line
                         if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                             System.out.println("Car enters line from NS");
                             System.out.println();
                         }
                         newCar = new Car(1, generateTurnNS());
                         //set arrival time for car
                         newCar.setArrive(bigTime);
                         NSQueue.addInOrder(newCar);
                     }//customer is in line
                     //generate the event for the next customer to arrive
                     deltaTimeArrive = TimeToArriveOrServe(.03);
                     //event time is current time plus big time
                     eventTime = bigTime +deltaTimeArrive;
                     if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                         System.out.println("The next car arrives at " + eventTime);
                         System.out.println();
                     }
                     //create event for next customer to arrive
                     //for now I'm having cars arrive back and forth between NS and EW - may change later
                     workEvent = new Event(1, eventTime);
                     numInEvent = EventQueue.addInOrder(workEvent);
                     break;
 
                 case 2://car arrives at EW
                     if ((busyEW == false) && (numInQueueEW <= 0)) {
                         //intersection is empty and no cars in line at NS
                         if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                             System.out.println("Car enters intersection from EW");
                             System.out.println();
                         }
                         newCar = new Car(2, generateTurnEW());
                         //set arrival time for car
                         newCar.setArrive(bigTime);
                         busyEW = true;
                         servedEW = newCar;
                         //generate the finished server event for this car
                         if(servedEW.getTurn() == 0){
                             // car turns north
                             deltaTimeServe = TimeToArriveOrServe(0.125);//1/8 seconds when turning north
                         }
                         else if(servedEW.getTurn() == 1){
                             //car turns south
                             deltaTimeServe = TimeToArriveOrServe(0.2);// 1/8 seconds when turning south
                         }
                         eventTime = deltaTimeServe+bigTime;
                         workEvent = new Event (8, eventTime);
                         //put this event in the event queue
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }//customer is in intersection
 
                     else if(busyEW == true){
                         //intersection is busy, put car in line
                         if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                             System.out.println("Car enters line from EW");
                             System.out.println();
                         }
                         newCar = new Car(2, generateTurnNS());
                         //set arrival time for car
                         newCar.setArrive(bigTime);
                         NSQueue.addInOrder(newCar);
 
                     }//customer is in line
                     //generate the event for the next customer to arrive
                     deltaTimeArrive = TimeToArriveOrServe(.03);
                     //event time is current time plus big time
                     eventTime = bigTime +deltaTimeArrive;
                     if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                         System.out.println("The next car arrives at " + eventTime);
                         System.out.println();
                     }
                     //create event for next customer to arrive
                     workEvent = new Event(2, eventTime);
                     numInEvent = EventQueue.addInOrder(workEvent);
                     break;
 
                 case 3://car enters intersection at EW
                     //decrement number in EW line
                     //generate completion time and departure event for this car
                     //set intersection to busy
                     numInQueueEW = EWQueue.getCount();
                     if((busyEW == false) &&(numInQueueEW > 0)){
                         //customer can enter intersection
                         if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                             System.out.println("First customer in EW line enters EW intersection");
                             System.out.println();
                         }
                         workCar = EWQueue.getValue(0);
                         EWThruLine++;
                         //delete car from line and put them in intersection
                         EWQueue.remove(0);
                         busyEW = true;
                         servedEW = workCar;
                         //generate finished server event for car
                         if(servedEW.getTurn() == 0){
                             // car turns north
                             deltaTimeServe = TimeToArriveOrServe(0.125);//1/8 seconds when turning north
                         }
                         else if(servedEW.getTurn() == 1){
                             //car turns south
                             deltaTimeServe = TimeToArriveOrServe(0.2);// 1/5 seconds when turning south
                         }
                         eventTime = deltaTimeServe + bigTime;
                         workEvent = new Event(8, eventTime);
                         numInEvent = EventQueue.addInOrder(workEvent);
 
                     }
                     else{
                         //either we are busy and have had event collision or there is no one in line
                         //System.out.println("In event 3 car enters intersection: unable to process event");
 
                     }
 
                     break;
 
                 case 4://car enters intersection at NS
                     //decrement number in NS line
                     //generate completion time and departure event for this car
                     //set intersection to busy
                     numInQueueNS = NSQueue.getCount();
                     if((busyNS == false) &&(numInQueueNS >0)){
                         //customer can enter intersection
                         if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                             System.out.println("First customer in NS line enters NS intersection");
                             System.out.println();
                         }
                         workCar = NSQueue.getValue(0);
                         NSThruLine++;
                         //delete car from line and put them in intersection
                         NSQueue.remove(0);
                         busyNS = true;
                         servedNS = workCar;
                         //generate finished server event for car
                         if(servedNS.getTurn() == 0){
                             // car turns north
                             deltaTimeServe = TimeToArriveOrServe(0.25);//1/4 seconds when turning north
                         }
                         else if(servedNS.getTurn() == 2){
                             //car turns south
                             deltaTimeServe = TimeToArriveOrServe(0.125);// 1/8 seconds when turning west
                         }
                         eventTime = deltaTimeServe + bigTime;
                         workEvent = new Event(7, eventTime);
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }
                     else{
                         //either we are busy and have had event collision or there is no one in line
                         System.out.println("In event 4 car enters intersection: unable to process event");
                     }
                     break;
 
                 case 5://EW light turns green, NS light turns red
                     //set light bool to TRUE and bootstrap next light event
                     green = true;
                     deltaTimeLight = 120;//this light will stay green for 120 seconds or 2 minutes
                     eventTime = deltaTimeLight + bigTime;
                     lightTime = eventTime;//this tells us when the light will switch
                     workEvent = new Event(6, eventTime);
                     numInEvent = EventQueue.addInOrder(workEvent);
 
                     break;
 
                 case 6://EW light turns red, NS light turns green
                     //set light bool to FALSE and bootstrap next light event
                     green = false;
                     deltaTimeLight = 180;//this light will stay red for 180 seconds or 3 minutes
 
                     eventTime = deltaTimeLight + bigTime;
                     lightTime = eventTime;//this tells us when the light will switch
                     workEvent = new Event(5, eventTime);
                     numInEvent = EventQueue.addInOrder(workEvent);
                     break;
 
                 case 7://car departs intersection at NS
                     //check for oncoming traffic
                     //if there is oncoming traffic we must wait until next light cycle
                     if(!green) {
                         if (((servedNS.getTurn() == 2) && (bigTime <= lightTime + 60))
                         || servedNS.getTurn() ==0){
                             //check if car is turning west and left turn arrow is on
                             // or if the car is continuing straight
                             busyNS = false;
                             NSThruSystem++;
                             numInQueueNS = NSQueue.getCount();
                             if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                                 System.out.println("Car leaves NS intersection - numInQueueEW = " + numInQueueNS);
                                 System.out.println();
                             }
                             if (numInQueueNS > 0) {
                                 //there are cars in line, generate a car enter intersection now at big time
                                 workEvent = new Event(4, bigTime + 0.01);
                                 //put this event in event queue
                                 numInEvent = EventQueue.addInOrder(workEvent);
                             }
 
                         }
                         else if (generateOncomingTraffic()) {
                             //if there is oncoming traffic turn arrow is off
                             //going north condition already checked
                             //if there is oncoming traffic, car must wait until next green arrow cycle
                             //System.out.println("ONCOMING TRAFFIC");
                             workEvent = new Event(7, lightTime+0.01);//moves this event to exactly when the light turns red
                             numInEvent = EventQueue.addInOrder(workEvent);
                             //which will then update again the next time this event is called
 
                         }
                         break;
                     }
                     else{
                         workEvent = new Event(7, lightTime+0.01);//moves this event to exactly when the light turns red
                         numInEvent = EventQueue.addInOrder(workEvent);
                         //System.out.println("RED LIGHT");
                 }
 
 
                     break;
 
                 case 8://car departs intersection at EW
                     //update number of customers through the system
                     //set intersection to not busy
                     //if there are people in line at EW generate an enter intersection event
                     if(green) {
                         busyEW = false;
                         EWThruSystem++;
                         numInQueueEW = EWQueue.getCount();
                         if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                             System.out.println("Car leaves EW intersection - numInQueueEW = " + numInQueueEW);
                             System.out.println();
                         }
                         if (numInQueueEW > 0) {
                             //there are cars in line, generate a car enter intersection now at big time
                             workEvent = new Event(3, (bigTime + .01));
                             //put this event in event queue
                             numInEvent = EventQueue.addInOrder(workEvent);
                         }
                     }
 
                     else{
                         //light is red
                         workEvent = new Event(8, lightTime+0.01);//moves this event to exactly when the light turns green
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }
                     break;
 
                 case 9://Sim shut down
                     System.out.println(" this event is type 9 and we are in the switch statement TROUBLE!");
                     System.out.println();
                     continue;
 
                 default:
                     System.out.println("this is a bad event type " + workEvent.getEventType() +
                             " at time " + workEvent.getTime());
             }//end of switch statement
             //this event is processed - delete it from event queue
             EventQueue.remove(0);
             //now to next event
             if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                 System.out.println("*****The Time is " + bigTime + "*****");
                 System.out.println();
             }
             workEvent = EventQueue.getValue(0);
             //System.out.println(workEvent.getEventType());
         }//end of event loop
         System.out.println("********PRINTING STATISTICS FOR THIS RUN*******");
         System.out.println();
         System.out.println("Average cars waiting in line on NS road: " + NSThruSystem/NSThruLine + " " + NSThruLine + "/" + NSThruSystem);
         System.out.println("Average cars waiting in line on EW road: " + EWThruSystem/EWThruLine + " " + EWThruLine + "/" + EWThruSystem);
         System.out.println("Average time waiting in line on EW road: " + EWTimeInLine/EWThruLine + " " + EWTimeInLine + "/" + EWThruLine);
         System.out.println("Average time waiting in line on NS road: " + NSTimeInLine/NSThruLine + " " + NSTimeInLine + "/" + NSThruLine);
 
 
     }//end of main
 
     public static int generateTurnEW(){
         int turn = 0;
         int x;//random variant
 
         x = (int) (Math.random() * 100);
         if(x<=30) turn = 0; //North
         else turn = 1; //South
         return turn;
     }//end of generateTurnEW
 
     public static int generateTurnNS(){
         int turn = 0;//default turn north
         int x;//random variant
 
         x = (int) (Math.random() * 100);
         if(x<=20) turn = 2;//West
         return turn;
     }
 
     public static boolean generateOncomingTraffic(){
         boolean traffic = false;
         int x;//random variant
 
         x = (int) (Math.random() * 100);
         if(x<=50) traffic = true;
         return traffic;
     }
 
     public static void PrintCustQueue(GenericManager<Car> MyQueue) {
         int numInQueue;
         Car workCar = new Car(0, 0);
         numInQueue = MyQueue.getCount() - 1;
 
 
         for (int i = 0; i <= numInQueue; i++) {
             workCar = MyQueue.getValue(i);
 
         }
     }//end of PrintCustQueue
 
     public static void PrintEventQueue(GenericManager<Event> MyQueue) {
         int numInEvent;
         Event workEvent = new Event(1, 1.0);
         numInEvent = MyQueue.getCount() - 1;
         System.out.println("Printing event queue there are " + numInEvent + " in event queue");
 
         for (int i = 0; i <= numInEvent; i++) {
             workEvent = MyQueue.getValue(i);
             System.out.println("This is the " + i + "event in the queue");
             System.out.println("Event type is " + workEvent.getEventType());
             System.out.println("Event time is " + workEvent.getTime());
         }
     }//end of PrintEventQueue
 
     public static double TimeToArriveOrServe(double rate) {
         //random process to determine time to arrive or the service time
         double deltaTime;
         double bigx;
         bigx = Math.random();
         while (bigx > 0.9) {
             bigx = Math.random();
         }
         deltaTime = -Math.log(1.0 - bigx) / rate;
         //System.out.println("DEBUG: in time to arrive with rate " + rate + " the delta time is " + deltaTime +
         // " bigx is " + bigx);
         return deltaTime;
     }//end of TimeToArriveOrServe
 
     public static double UpdateCustomer(GenericManager<Car> carLine, double deltaTime) {
         //this function adds all time spent for a customer in line for deltatime
         double lineTime = 0.0;
         int carInLine;
         carInLine = carLine.getCount();
 
         if (carInLine == 0) {
             return lineTime;
         } else {
             lineTime = deltaTime * carInLine;
         }
         return lineTime;
     }//end of UpdateCustomer
 
     public static double UpdateServers(Car s1, Car s2, boolean b1, boolean b2,
                                        double deltaTime) {
         //this function updates the time to customers in servers
         double serveTime = 0.0;
         if (b1 && b2) {
             return serveTime = 2 * deltaTime;
         } else if (b1 || b2) {
             serveTime = deltaTime;
         }
         return serveTime;
     }//end of UpdateServers
 }//end of StopLight
 class Car implements Comparable {
     /*Car class stores time the customer enters line, time in server
         and time in system
          */
     protected double timeInLine;
     protected double timeInServer;
     protected double timeInSystem;
     protected double timeArrive;
     protected int roadArrive;//int to determine what road the car arrives at: 1 for NS, 2 for EW
     protected int turn;//what direction the car is turning in
 
     //create customer constructor
     public Car(int r, int t) {
         timeInLine = timeInServer = timeInSystem = 0;
         roadArrive = r;
         turn = t;
     }//end of customer constructor
 
     public int compareTo(Object o) {
         if((getTimeInLine()>((Car)o).getTimeInLine())){
             return 1;
         }
         else if(getTimeInLine()<((Car)o).getTimeInLine()){
             return -1;
         }
 
         return 0;
     }//end of compareTo
 
     public void setArrive(double x) {
         //the time of arrival is set from x
         timeArrive = x;
     }//end of set arrive
 
     public void setInLine(double x) {
         //we add value of x. It is the del time
         timeInLine += x;
     }//end of setInLine
 
     public void setInServer(double x) {
         timeInServer += x;
     }//end of setInServer
 
     public void setRoadArrive(int x){
         roadArrive = x;
     }
 
     public void setTurn(int x){
         turn = x;
     }
 
 
     //getter methods
     public double getTimeInLine() {
         return timeInLine;
     }
 
     public int getTurn(){
         return turn;
     }
 
     public double getTimeInServer() {
         return timeInServer;
     }
 
     public double getTimeInSystem() {
         return timeInSystem;
     }
 
     public double getTimeArrive() {
         return timeArrive;
     }
 
     public int getRoadArrive() {
         return roadArrive;
     }
 }//end of Customer class
 class Event implements Comparable {
     /* Event holds event type and event time
      */
     protected int eventType; //event type
     protected double time; //time of the event
 
     public Event(int e, double t) {
         eventType = e;
         time = t;
     }//end of Event constructor
 
     public int compareTo(Object o) {
         if (getTime() > ((Event) o).getTime()) {
             return 1;//if time a > time b
         } else if (getTime() < ((Event) o).getTime()) {
             return -1;//if time a < time b
         } else return 0;
     }//end of compareTo
 
     public int getEventType() {
         return eventType;
     }
 
     public double getTime() {
         return time;
     }
 
 }//end of Event
 
 class GenericManager<T extends Comparable> {
     protected ArrayList<T> myList = new ArrayList<T>();
     protected int count;//next available value in list
 
     public GenericManager() {
         count = 0;
     }//end of constructor
 
     public int addAtEnd(T x) {
         //places values at the end of list
         myList.add(count++, x);
         return count;
     }//end of addAtEnd
 
     public int getCount() {
         return count;
     }
 
     public int addInOrder(T x) {
         int i;
         //this adds objects from smallest to largest
         if ((count == 0) || ((x.compareTo(myList.get(0))) == -1)
                 || (x.compareTo(myList.get(0)) == 0)) {
             //if less than or equal to first entry
             myList.add(0, x);
         } else if ((x.compareTo(myList.get(count - 1)) == 1
                 || (x.compareTo(myList.get(count - 1)) == 0))) {
             //x is greater than last entry
             myList.add(count, x);
         } else {
             //the object is greater than the first and less than the last
             i = 0;
             while ((i < count) && (x.compareTo(myList.get(i)) == 1)) {
                 i++;
             }
             myList.add(i, x);
         }
         count++;
         return count;
     }//end of addInOrder
 
     public int addAtFront(T x) {
         // add object to front of list
         myList.add(0, x);
         count++;
         return count;
     }//end of addAtFront
 
     public T getValue(int i) {
         // gets value from list
         if (i < count) return myList.get(i);
         else {
             return myList.get(0);
         }
     }//end of getValue
 
     public void manageAndSort() {
         //generic sort that sorts everything but the objects
         //will sort an array of flat objects based on compareTo func
         T xsave, ysave, a, b;
         int isw = 1, xlast = myList.size();
         while (isw == 1) {
             isw = 0;
             for (int i = 0; i <= xlast - 2; i++) {
                 a = myList.get(i);
                 b = myList.get(i + 1);
                 switch (a.compareTo(b)) {
                     case 1://objects are in proper order
                         break;
                     case -1://objects are out of order
                         xsave = myList.get(i);
                         ysave = myList.get(i + 1);
                         myList.remove(i);
                         myList.add(i, ysave);
                         myList.remove(i + 1);
                         myList.add((i + 1), xsave);
                         isw = 1;
                         break;
                     default://objects are equal
                 }//end of switch
             }//end of for
         }//end of while
     }//end of manageAndSort
 
     public void remove(int i) {
         //removes value from index i
         if ((i >= 0) && (i <= count - 1)) {
             myList.remove(i);
             count--;
         }
     }//end of remove
 }//end of  GenericManager