import java.util.*;
import java.io.*; 


public class ParkingManager {

    static int currentYear = 2025; 
    static int currentMonth = 1;

    public static void main(String[] args) throws IOException{ 
        Scanner in = new Scanner(new File("parking.txt"), "UTF-8");
       
        int maxCapacity = in.nextInt();
        int regularFee = in.nextInt();
        int visitorFee = in.nextInt();
        int regularMemNum = in.nextInt(); 

        in.nextLine(); 
        
        Parking place = new Parking(maxCapacity, regularFee, visitorFee, regularMemNum);
        place.loadRegularMember(in, regularMemNum, currentYear, 3);
        


        Scanner input = new Scanner (System.in); 
        int vehicleNumber;
        int year ; 
        int month ; 
        int day ; 
        int hour; 
        int minute ; 
        String type; 
        int attr; 
        while(true){
            System.out.print("> "); 
            String[] cmd = input.nextLine().split(" "); 
            switch(cmd[0]){
                case "e": 
                    vehicleNumber = Integer.parseInt(cmd[1]); 
                    year = Integer.parseInt(cmd[2]); 
                    month = Integer.parseInt(cmd[3]); 
                    day = Integer.parseInt(cmd[4]); 
                    hour = Integer.parseInt(cmd[5]); 
                    minute = Integer.parseInt(cmd[6]); 

                    if(!place.isRegular(vehicleNumber)){
                        type = cmd[7]; 
                        attr = Integer.parseInt(cmd[8]); 
                        place.enter(vehicleNumber, year, month, day, hour, minute, type, attr); 
                    } else{
                        place.enter(vehicleNumber, year, month, day, hour, minute); 
                    }
                    break; 
                case "x": 
                    vehicleNumber = Integer.parseInt(cmd[1]); 
                    year = Integer.parseInt(cmd[2]); 
                    month = Integer.parseInt(cmd[3]); 
                    day = Integer.parseInt(cmd[4]); 
                    hour = Integer.parseInt(cmd[5]); 
                    minute = Integer.parseInt(cmd[6]);  
                    place.exit(vehicleNumber, year, month, day, hour, minute); 
                    break; 
                case "v": 
                    place.view(); 
                    break; 
                case "i":
                    year = Integer.parseInt(cmd[1]); 
                    month = Integer.parseInt(cmd[2]); 
                    place.income(year,month); 
                    break; 
                case "r": 
                    vehicleNumber = Integer.parseInt(cmd[1]); 
                    year = Integer.parseInt(cmd[2]); 
                    month = Integer.parseInt(cmd[3]); 
                    day = Integer.parseInt(cmd[4]); 
                    String id = cmd[5]; 
                    String dept = cmd[6]; 
                    String name = cmd[7]; 
                    type = cmd[8]; 
                    attr = Integer.parseInt(cmd[9]); 
                    place.registerRegular(vehicleNumber, year, month, day, id, dept, name, type, attr); 
                    break; 
                case "a": 
                    place.viewRegisteredRegularMember(); 
                    break; 
                default:
                    System.out.println("알 수 없는 명령입니다.");
                    break; 
                    
            }
        }
    }

    public static int calculate_time_difference(DateTime in, DateTime out){ 
        int totalInMinutes = in.year * 525600 + in.month * 43200 + in.day * 1440 + in.hour * 60 + in.minute;
        int totalOutMinutes = out.year * 525600 + out.month * 43200 + out.day * 1440 + out.hour * 60 + out.minute;
        return totalOutMinutes - totalInMinutes;
    }


    public static boolean isValidDate(int year, int month, int day, int hour, int minute ){
        if (hour < 0 || hour >= 24 || minute < 0 || minute >= 60) return false;
        if (month < 1 || month > 12) return false;

        int[] daysInMonth = {31, isLeapYear(year) ? 29 : 28, 31, 30, 31, 30,
                         31, 31, 30, 31, 30, 31};

        return day >= 1 && day <= daysInMonth[month - 1];
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
   
    static class DateTime { 
        int year, month, day, hour, minute; 
    }

    static class RegularMember{
        private int vehicleNumber; 
        private String id; 
        private String dept; 
        private String name; 
        private Vehicle vehicle; 
        private int year, month, day; 
        private int newRegularMemberFee; 

        RegularMember(int vehicleNumber, String id, String dept, String name, Vehicle vehicle, int year, int month, int day, int fee){ 
            this.vehicleNumber = vehicleNumber; 
            this.id = id; 
            this.dept = dept; 
            this.name = name; 
            this.vehicle = vehicle; 
            this.year = year; 
            this.month = month; 
            this.day = day; 

            int semesterEndMonth = (month <= 6) ? 6 : 12;
            int months = semesterEndMonth - month + 1;
            double rate = vehicle.getRate(); 
            if (vehicle instanceof HybridCar){
                this.newRegularMemberFee = (int)((fee * months / 6.0) * (rate / 2.0)); 
            } else {
                this.newRegularMemberFee = (int)((fee * months / 6.0) * rate); 
            }
        }

        
        public Vehicle getVehicle(){
            return vehicle; 
        }
      
    }

    static class ParkedVehicle{ 
        int vehicleNumber; 
        DateTime inTime;
        boolean isRegular; 
        Vehicle vehicle; 

        ParkedVehicle(int vehicleNumber, DateTime inTime, boolean isRegular){ 
            this.vehicleNumber = vehicleNumber; 
            this.inTime = inTime; 
            this.isRegular = isRegular; 
        }

        ParkedVehicle(int vehicleNumber, DateTime inTime, boolean isRegular, Vehicle vehicle){
            this(vehicleNumber, inTime, isRegular); 
            this.vehicle = vehicle; 
        } //overloaded constructor for visitor vehicles 
    }

    static class VisitRecord{ 
        int vehicleNumber; 
        DateTime inTime, outTime; 
        int fee; 
        
        VisitRecord(int vehicleNumber, DateTime inTime, DateTime outTime, int fee){ 
            this.vehicleNumber = vehicleNumber; 
            this.inTime = inTime; 
            this.outTime = outTime; 
            this.fee = fee; 
        }    
    }
    

    static class Parking{
        int maxCapacity; 
        int regularFee; 
        int visitorFee;
        int regularMemNum = 0;  
        int visitCount = 0; 
        int parkedCount = 0; 
    
        RegularMember[] regularMemList; 
        ParkedVehicle[] parkedVehiclesList = new ParkedVehicle[1000]; 
        VisitRecord[] visitRecordsList = new VisitRecord[1000]; 

        Parking(int capacity, int regularFee, int visitorFee, int regularSize){
            this.maxCapacity = capacity; 
            this.regularFee = regularFee; 
            this.visitorFee = visitorFee; 
            this.regularMemList = new RegularMember[regularSize]; 
        }

        public void enter(int vehicleNumber, int year, int month, int day, int hour, int minute){ 
            if (!ParkingManager.isValidDate(year, month, day, hour, minute)) {
                System.out.println("잘못된 시간 정보입니다!");
                return;
            }

            for (int i = 0; i < parkedCount; i++) {
                if (parkedVehiclesList[i].vehicleNumber == vehicleNumber) {
                    String type = parkedVehiclesList[i].isRegular ? "정기주차" : "방문주차";
                    System.out.println(type + " 차량 " + vehicleNumber + "는(은) 이미 입차한 차량입니다!");
                    return;
                }
            }

            if (parkedCount >= maxCapacity){ 
                System.out.println("공간 부족으로 입차할 수 없습니다."); 
                return; 
            }
        
            DateTime inTime = new DateTime(); 
            inTime.year = year; 
            inTime.month = month; 
            inTime.day = day;
            inTime.hour = hour; 
            inTime.minute = minute; 

            boolean isReg = isRegular(vehicleNumber); 
            parkedVehiclesList[parkedCount++] = new ParkedVehicle(vehicleNumber, inTime, isReg);

            if (isReg) {
                System.out.println("정기주차 차량 " + vehicleNumber + "가(이) 입차하였습니다!");
            } else {
                System.out.println("방문주차 차량 " + vehicleNumber + "가(이) 입차하였습니다!");
            } 
        }

        public void enter(int vehicleNumber, int year, int month, int day, int hour, int minute, String type, int attr){
            if(!ParkingManager.isValidDate(year, month, day, hour, minute)){
                System.out.println("잘못된 시간 정보입니다!"); 
                return; 
            }

            for (int i = 0; i < parkedCount; i++){
                if (parkedVehiclesList[i].vehicleNumber == vehicleNumber){
                    System.out.println("방문주차 차량 " + vehicleNumber + "는(은) 이미 입차한 차량입니다!"); 
                    return; 
                }
            }

            if (parkedCount >= maxCapacity){
                System.out.println("공간 부족으로 입차할 수 없습니다."); 
                return; 
            }

            DateTime inTime = new DateTime(); 
            inTime.year = year; 
            inTime.month = month; 
            inTime.day = day; 
            inTime.hour = hour; 
            inTime.minute = minute; 

            Vehicle vehicle; 
            switch (type){
                case "g": 
                    vehicle = new GasCar(String.valueOf(vehicleNumber), attr); 
                    break; 
                case "h": 
                    vehicle = new HybridCar(String.valueOf(vehicleNumber), attr); 
                    break; 
                case "b": 
                    vehicle = new Bus(String.valueOf(vehicleNumber), attr); 
                    break; 
                case "t": 
                    vehicle = new Truck(String.valueOf(vehicleNumber), attr); 
                    break; 
                default: 
                    System.out.println("알 수 없는 차량 종류입니다!"); 
                    return; 
            }

            parkedVehiclesList[parkedCount++] = new ParkedVehicle(vehicleNumber, inTime, false, vehicle); 
            System.out.println("방문주차 차량 " + vehicleNumber + "가(이) 입차하였습니다!"); 
        }

        public void exit(int vehicleNumber, int year, int month, int day, int hour, int minute){ 
            if (!ParkingManager.isValidDate(year, month, day, hour, minute)) {
                System.out.println("잘못된 시간 정보입니다!");
                return;
            }

            DateTime outTime = new DateTime(); 
            outTime.year = year; 
            outTime.month = month; 
            outTime.day = day;
            outTime.hour = hour; 
            outTime.minute = minute; 


            int index = -1; 
            for (int i = 0 ; i < parkedCount; i++){
                if(parkedVehiclesList[i].vehicleNumber == vehicleNumber){ 
                    index = i; 
                    break; 
                }
            }
            if (index == -1){ 
                System.out.println("입차하지 않은 차량입니다!"); 
                return; 
            }

            ParkedVehicle vehicle = parkedVehiclesList[index]; 

            int fee = 0; 

            if(vehicle.isRegular){ 
                System.out.println("정기주차 차량 " + vehicleNumber + "가(이) 출차하였습니다!");
            } else {
                int minutes = calculate_time_difference(vehicle.inTime, outTime); 
                fee = vehicle.vehicle.calculateFee(minutes, visitorFee); 

                System.out.println("방문주차 차량 " + vehicleNumber + "가(이) 출차하였습니다!");
                System.out.println("주차시간: " + minutes + "분");
                System.out.println("주차요금: " + fee + "원");
            }

            visitRecordsList[visitCount++] = new VisitRecord(vehicleNumber, vehicle.inTime, outTime, fee);

            for (int i = index; i < parkedCount - 1; i++){ 
                parkedVehiclesList[i] = parkedVehiclesList[i+1]; 
            }
            parkedVehiclesList[--parkedCount] = null; 
        }

        public void view(){ 
            System.out.println("정기주차 차량목록"); 

            ParkedVehicle[] regularList = new ParkedVehicle[parkedCount]; 
            int regCount = 0; 

            for (int i = 0; i < parkedCount; i++){ 
                if (parkedVehiclesList[i].isRegular){
                    regularList[regCount++] = parkedVehiclesList[i]; 
                }
            }
            for (int i = 0; i < regCount - 1; i++) {
                for (int j = i + 1; j < regCount; j++) {
                    if (regularList[i].vehicleNumber > regularList[j].vehicleNumber) {
                        ParkedVehicle temp = regularList[i];
                        regularList[i] = regularList[j];
                        regularList[j] = temp;
                    }
                }
            }
        
            int index = 1; 
            for (int i = 0; i < regCount; i++) {
                ParkedVehicle pv = regularList[i];
                RegularMember m = getRegularMember(pv.vehicleNumber);
                if (m != null) {
                    System.out.printf("  %d %04d  %d %02d %02d %02d %02d  %s %s %s %s\n",
                        index++, pv.vehicleNumber,
                        pv.inTime.year, pv.inTime.month, pv.inTime.day,
                        pv.inTime.hour, pv.inTime.minute,
                        m.id, m.dept, m.name, m.getVehicle().getAttribute());
                } 
            }

            System.out.println("방문주차 차량목록");

            ParkedVehicle[] visitorList = new ParkedVehicle[parkedCount]; 
            int visCount = 0; 

            for (int i = 0; i < parkedCount; i++) {
                if (!parkedVehiclesList[i].isRegular) {
                    visitorList[visCount++] = parkedVehiclesList[i];
                }
            }
            for (int i = 0; i < visCount - 1; i++) {
                for (int j = i + 1; j < visCount; j++) {
                    if (visitorList[i].vehicleNumber > visitorList[j].vehicleNumber) {
                        ParkedVehicle temp = visitorList[i];
                        visitorList[i] = visitorList[j];
                        visitorList[j] = temp;
                    }
                }
            } 

            index = 1; 
            for (int i = 0; i < visCount; i++) {
                ParkedVehicle pv = visitorList[i];
                System.out.printf("  %d %04d  %d %02d %02d %02d %02d %s\n",
                    index++, pv.vehicleNumber,
                    pv.inTime.year, pv.inTime.month, pv.inTime.day,
                    pv.inTime.hour, pv.inTime.minute, pv.vehicle.getAttribute());
            }
        }

        public void income(int year, int month) {
            int activeRegulars = 0;
            int visitorIncome = 0;

            int monthlyFee = regularFee / 6;
            double regularIncome = 0; 


          
            for (int i = 0; i < regularMemNum; i++) {
                RegularMember m = regularMemList[i];
                if (year > m.year || (year == m.year && month >= m.month)) {
                    activeRegulars++;
                    Vehicle v = m.getVehicle(); 
                    double rate = v.getRate();
                    if (v instanceof HybridCar){
                        regularIncome += (monthlyFee * rate / 2.0);
                    } else {
                        regularIncome += (monthlyFee * rate); 
                    }
                }
            }


            for (int i = 0; i < visitCount; i++) {
                VisitRecord vr = visitRecordsList[i];
                if (vr.outTime.year == year && vr.outTime.month == month) {
                    visitorIncome += vr.fee;
                }
            }

            int total = (int)(regularIncome + visitorIncome);
            System.out.printf("총수입(%d년 %d월) : %,d원\n", year, month, total);
            System.out.printf("  - 정기주차 차량: %,d원\n", (int)regularIncome);
            System.out.printf("  - 방문주차 차량: %,d원\n", visitorIncome);
        }


        public void registerRegular(int vehicleNumber, int year, int month, int day, String id, String dept, String name, String type, int attr){
            for (int i = 0; i < this.regularMemNum; i++){
                if(this.regularMemList[i].vehicleNumber == vehicleNumber){
                    System.out.println("이미 등록된 차량입니다!");
                    return; 
                }
            }
            if (this.regularMemNum >= this.regularMemList.length){
                RegularMember[] newList = new RegularMember[this.regularMemList.length + 10]; 
                System.arraycopy(this.regularMemList, 0, newList, 0, this.regularMemList.length); 
                this.regularMemList = newList; 
            }

            Vehicle vehicle; 
            switch (type){
                case "g": 
                    vehicle = new GasCar(String.valueOf(vehicleNumber), attr); 
                    break; 
                case "h": 
                    vehicle = new HybridCar(String.valueOf(vehicleNumber), attr); 
                    break; 
                case "b": 
                    vehicle = new Bus(String.valueOf(vehicleNumber), attr); 
                    break; 
                case "t": 
                    vehicle = new Truck(String.valueOf(vehicleNumber), attr); 
                    break; 
                default: 
                    System.out.println("알 수 없는 차량 종류입니다!"); 
                    return; 
            }

            RegularMember newMember = new RegularMember(vehicleNumber, id, dept, name, vehicle, year, month, day, this.regularFee);
            this.regularMemList[this.regularMemNum++] = newMember; 
            System.out.printf("정기주차 차량(이) 등록되었습니다.\n");
        }

        public void viewRegisteredRegularMember(){
            if(this.regularMemNum == 0){
                System.out.println("등록된 정기주차 차량이 없습니다. "); 
                return; 
            }
            RegularMember[] currentMembers = new RegularMember[this.regularMemNum]; 
            for (int i = 0; i < this.regularMemNum; i++){
                currentMembers[i] = this.regularMemList[i]; 
            }
            Arrays.sort(currentMembers, (a, b) -> Integer.compare(a.vehicleNumber, b.vehicleNumber));

            int index = 1; 
            for(RegularMember m : currentMembers){
                System.out.printf("  %d %04d %d-%02d-%02d %s %s %s %s \n",
                    index++, m.vehicleNumber,
                    m.year, m.month, m.day, 
                    m.id, m.dept, m.name, m.vehicle.getAttribute());
            }
        }

        public void loadRegularMember(Scanner in, int count, int currentYear, int startMonth){
            for (int i = 0; i < count; i++) {
                int vehicleNumber = in.nextInt();         
                String id = in.next();
                String dept = in.next(); 
                String name = in.next(); 
                String type = in.next(); 
                int attr = in.nextInt(); 

                Vehicle vehicle; 
                switch (type){
                    case "g": 
                        vehicle = new GasCar(String.valueOf(vehicleNumber), attr); 
                        break; 
                    case "h": 
                        vehicle = new HybridCar(String.valueOf(vehicleNumber), attr); 
                        break; 
                    case "b": 
                        vehicle = new Bus(String.valueOf(vehicleNumber), attr); 
                        break; 
                    case "t": 
                        vehicle = new Truck(String.valueOf(vehicleNumber), attr); 
                        break; 
                    default: 
                        System.out.println("알 수 없는 차량 종류입니다!"); 
                        return; 
                }

                this.regularMemList[i] = new RegularMember(vehicleNumber, id, dept, name, vehicle, currentYear, startMonth, 1, this.regularFee);
            }
            this.regularMemNum = count; 
        }

        public boolean isRegular(int vehicleNumber){ 
            for (int i = 0; i < regularMemList.length; i++){
                if(regularMemList[i].vehicleNumber == vehicleNumber){ 
                    return true; 
                }
            }
            return false; 
        }

        public RegularMember getRegularMember(int vehicleNumber){ 
            for (int i =0; i < regularMemList.length; i++){ 
                if(regularMemList[i].vehicleNumber == vehicleNumber){
                    return regularMemList[i]; 
                }
            }
            return null; 
        }
    
    }

    public static abstract class Vehicle{
        private String type; 
        private String vehicleNumber; 

        public Vehicle(String vehicleNumber, String type){
            this.vehicleNumber = vehicleNumber; 
            this.type = type; 
        }
      
        public abstract int calculateFee(int minutes, int unitFee); 
        public abstract String getAttribute(); 
        public abstract double getRate();
    }

    public static class GasCar extends Vehicle{
        private int displacement;

        public GasCar(String vehicleNumber, int displacement){
            super(vehicleNumber, "g"); 
            this.displacement = displacement; 
        }

        @Override
        public double getRate() {
            if (displacement < 1000) return 0.8;
            else if (displacement < 2000) return 1.0;
            else if (displacement < 3000) return 1.2;
            else return 1.5;
        }

        @Override
        public int calculateFee(int minutes, int unitFee){
            int baseFee = (int)Math.ceil(minutes/10.0)*unitFee; 
            return (int)(baseFee * getRate()); 
        }

        @Override
        public String getAttribute(){
            return "가솔린 " + displacement;
        }
    }

    public static class HybridCar extends Vehicle{
        private int displacement; 

        public HybridCar(String vehicleNumber, int displacement){
            super(vehicleNumber, "h"); 
            this.displacement = displacement; 
        }

        @Override
        public double getRate() {
            if (displacement < 1000) return 0.8;
            else if (displacement < 2000) return 1.0;
            else if (displacement < 3000) return 1.2;
            else return 1.5;
        }

        @Override
        public int calculateFee(int minutes, int unitFee){
            int baseFee = (int)Math.ceil(minutes/10.0) * unitFee; 
            return (int)((baseFee * getRate()) / 2.0); 
        }

        @Override
        public String getAttribute(){
            return "하이브리드 " + displacement; 
        }
    }

    public static class Bus extends Vehicle{
        private int maxPassenger; 

        public Bus(String vehicleNumber, int maxPassenger){
            super(vehicleNumber, "b"); 
            this.maxPassenger = maxPassenger; 
        }

        @Override
        public double getRate(){
            if (maxPassenger < 12) return 1.0; 
            else if (maxPassenger < 20) return 1.2; 
            else if (maxPassenger < 30) return 1.5;
            else return 2.0; 
        }

        @Override
        public int calculateFee(int minutes, int unitFee){
            int baseFee = (int)Math.ceil(minutes/10.0) * unitFee; 
            return (int)(baseFee * getRate()); 
        }

        @Override
        public String getAttribute(){
            return "버스 " + maxPassenger; 
        }
    }

    public static class Truck extends Vehicle{
        private double ton; 

        public Truck(String vehicleNumber, double ton){
            super(vehicleNumber, "t"); 
            this.ton = ton; 
        }

        @Override
        public double getRate(){
            if (ton < 1.0) return 1.0; 
            else if (ton < 2.0) return 1.5; 
            else if (ton < 4.0) return 2.0; 
            else return 3.0; 
        }

        @Override
        public int calculateFee(int minutes, int unitFee){
            int baseFee = (int)Math.ceil(minutes/10.0) * unitFee; 
            return (int)(baseFee * getRate());
        }

        @Override
        public String getAttribute(){
            return "트럭 " + ton; 
        }
    }
    

}



    