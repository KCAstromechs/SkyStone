package org.firstinspires.ftc.teamcode;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import static android.content.Context.SENSOR_SERVICE;

@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public class RobotBaseGabriel implements SensorEventListener {

    DcMotor frontRight, frontLeft, backRight, backLeft, encoderWheelY, encoderWheelX, lift, flip;

    Servo foundRight, foundLeft, mainFlop, subFlop, release;

    OpMode callingOpMode;

    private double ticksPerRotation = 488;
    private double ticksPerRotationTetrix = 1400;

    private double wheelDiameter = 4;

    double ticksPerInch = ((ticksPerRotation)/(wheelDiameter * Math.PI));
    double ticksPerInchTetrix = ((ticksPerRotationTetrix)/(3 * Math.PI));

    protected static final double P_DRIVE_COEFF = 0.042;

    static final double driveSpeed = 0.9;

    int frontRightBaseEncoder, frontLeftBaseEncoder, backRightBaseEncoder, encoderWheelYBaseEncoder, encoderWheelXBaseEncoder, backLeftBaseEncoder = 0;


    private DistanceSensor distSensor;
    //variables for gyro operation
    private float zero;
    private float rawGyro;
    public int sensorDataCounter = 0;

    //arrays for gyro operation
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    //objects for gyro operation
    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor;

    protected boolean hasBeenZeroed= false;

    float zRotation;

    double global_y1, global_y2, global_x1, global_x2;
    double global_theta;
    double global_posF, global_posS;
    double globalX;
    double globalY;
    double global_deltaPosForwards = 0;
    double global_deltaPosSideways = 0;
    double global_lastForwardPos = 0;
    double global_lastSidewaysPos = 0;

    int pos = 1;

    public RobotBaseGabriel(OpMode _callingOpMode) {
        callingOpMode = _callingOpMode;

        frontRight = callingOpMode.hardwareMap.dcMotor.get("frontRight");
        frontLeft = callingOpMode.hardwareMap.dcMotor.get("frontLeft");
        backRight = callingOpMode.hardwareMap.dcMotor.get("backRight");
        backLeft = callingOpMode.hardwareMap.dcMotor.get("backLeft");
        encoderWheelY = callingOpMode.hardwareMap.dcMotor.get("encoderWheelY");
        encoderWheelX = callingOpMode.hardwareMap.dcMotor.get("encoderWheelX");
        lift = callingOpMode.hardwareMap.dcMotor.get("lift");
        flip = callingOpMode.hardwareMap.dcMotor.get("flip");

        foundLeft  = callingOpMode.hardwareMap.servo.get("foundationLeft");
        foundRight = callingOpMode.hardwareMap.servo.get("foundationRight");
        mainFlop   = callingOpMode.hardwareMap.servo.get("mainFlop");
        subFlop    = callingOpMode.hardwareMap.servo.get("subFlop");
        release    = callingOpMode.hardwareMap.servo.get("release");

        distSensor = callingOpMode.hardwareMap.get(DistanceSensor.class, "distSensor");

        frontRight.setDirection(DcMotor.Direction.REVERSE);
        backRight.setDirection(DcMotor.Direction.REVERSE);


        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoderWheelY.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoderWheelX.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flip.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        encoderWheelY.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        encoderWheelX.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        flip.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        release.setPosition(0);
        foundRight.setPosition(0);
        foundLeft.setPosition(0.75);
        mainFlop.setPosition(0.7);
        subFlop.setPosition(0.35);

        mSensorManager = (SensorManager) _callingOpMode.hardwareMap.appContext.getSystemService(SENSOR_SERVICE);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
        hasBeenZeroed = false;
        globalY = 0;
        globalX = 0;
    }

    public void updateDriveMotors(double frontleft, double frontright, double backleft, double backright) {
        frontRight.setPower(frontright);
        backRight.setPower(backright);
        frontLeft.setPower(frontleft);
        backLeft.setPower(backleft);
    }

    public void stopAndReset() throws InterruptedException {

        frontRight.setPower(0);
        backRight.setPower(0);
        frontLeft.setPower(0);
        backLeft.setPower(0);
        Thread.sleep(200);

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoderWheelY.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoderWheelX.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        encoderWheelY.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        encoderWheelX.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        Thread.sleep(10);
    }

    public void stop() throws InterruptedException {

        frontRight.setPower(0);
        backRight.setPower(0);
        frontLeft.setPower(0);
        backLeft.setPower(0);
        Thread.sleep(200);
    }

    public void setEncoderBase(){
        frontRightBaseEncoder = frontRight.getCurrentPosition();
        frontLeftBaseEncoder = frontLeft.getCurrentPosition();
        backRightBaseEncoder = backRight.getCurrentPosition();
        backLeftBaseEncoder = backLeft.getCurrentPosition();
        encoderWheelYBaseEncoder = encoderWheelY.getCurrentPosition();
        encoderWheelXBaseEncoder = encoderWheelX.getCurrentPosition();
    }

    public void updateGlobalPosition(){
        //global coordinates calculation
        global_posF = encoderWheelY.getCurrentPosition();
        global_posS = -encoderWheelX.getCurrentPosition();

        global_deltaPosForwards = (global_posF - global_lastForwardPos);
        global_deltaPosSideways = (global_posS - global_lastSidewaysPos);

        global_lastForwardPos = global_posF;
        global_lastSidewaysPos = global_posS;

        global_theta = zRotation * Math.PI / 180.0;
        global_y1 = global_deltaPosForwards * Math.cos(global_theta);
        global_y2 = -global_deltaPosSideways * Math.sin(global_theta);
        global_x1 = global_deltaPosForwards * Math.sin(global_theta);
        global_x2 = global_deltaPosSideways * Math.cos(global_theta);

        // these represent the current x,y in global coordinates ... in inches
        globalY += ticksToInches(global_y1 + global_y2);
        globalX += ticksToInches(global_x1 + global_x2);
    }

    public int getCurrentFrontRightPosition() {return Math.abs(frontRight.getCurrentPosition()-frontRightBaseEncoder);}

    public int getCurrentFrontLeftPosition() {return Math.abs(frontLeft.getCurrentPosition()-frontLeftBaseEncoder);}

    public int getCurrentBackRightPosition() {return Math.abs(backRight.getCurrentPosition()-backRightBaseEncoder);}

    public int getCurrentBackLeftPosition() {return Math.abs(backLeft.getCurrentPosition()-backLeftBaseEncoder);}

    public int getCurrentAveragePosition() {return Math.abs((getCurrentBackLeftPosition()+getCurrentBackRightPosition()+getCurrentFrontRightPosition())/3);} //frontLeft is MIA

    public int getEncoderWheelYPosition() {return encoderWheelY.getCurrentPosition()-encoderWheelYBaseEncoder;}

    public int getEncoderWheelXPosition() {return -1*(encoderWheelX.getCurrentPosition()-encoderWheelXBaseEncoder);}

    public double getDistSensorInch() {return distSensor.getDistance(DistanceUnit.INCH);}

    public double ticksToInches(double x) {
        return 3 * Math.PI * x/1400; //x/(1400/3*pi)
    }

    public void driveStraight(double inches, float heading) throws InterruptedException { driveStraight(inches, heading, driveSpeed); }

    public void driveStraight(double inches, float heading, double speedLimit)  throws InterruptedException {
        double error;                                           //The number of degrees between the true heading and desired heading
        double correction;                                      //Modifies power to account for error
        double leftPower;                                       //Power being fed to left side of bot
        double rightPower;                                      //Power being fed to right side of bot
        double max;                                             //To be used to keep powers from exceeding 1
        double P_COEFF = 0.0012;      //0.002
        double I_COEFF = 0.00192;     //0.0035
        double D_COEFF = 0.0003;    //0.00042
        if(inches<24){
            P_COEFF = 0.0015;
            I_COEFF = 0.00192;
            D_COEFF = 0.0003;
        }
        double power;
        double deltaT;
        double derivative = 0;
        double integral = 0;
        int deltaD;
        int lastEncoder;
        int distance;
        long loops = 0;
        heading = (int) normalize360(heading);

        setEncoderBase();
        lastEncoder = 0;

        int target = (int) (inches * ticksPerInchTetrix);

        speedLimit = Range.clip(speedLimit, 0.0, 1.0);

        if (speedLimit==0){
            return;
        }

        double lastTime = callingOpMode.getRuntime();

        while ((((Math.abs(target)-50) > Math.abs(getEncoderWheelYPosition()) || ((Math.abs(target)+50) < Math.abs(getEncoderWheelYPosition())))
                || (loops==0 || Math.abs(derivative)<1)) && ((LinearOpMode) callingOpMode).opModeIsActive()) {

            error = heading - zRotation;

            distance = Math.abs(target) - Math.abs(getEncoderWheelYPosition());

            while (error > 180) error = (error - 360);
            while (error <= -180) error = (error + 360);

            correction = Range.clip(error * P_DRIVE_COEFF, -1, 1);

            deltaT = callingOpMode.getRuntime()-lastTime;
            lastTime = callingOpMode.getRuntime();
            deltaD = getEncoderWheelYPosition()-lastEncoder;
            lastEncoder = getEncoderWheelYPosition();

            derivative = ((double) deltaD)/deltaT;

            if(Math.abs(distance*P_COEFF)<1){
                integral += distance*deltaT;
            } else {
                integral = 0;
            }

            power = (distance*P_COEFF) + (integral*I_COEFF) - (derivative*D_COEFF);

            if (Math.abs(power) > Math.abs(speedLimit)) {
                power /= Math.abs(power);
                power *= Math.abs(speedLimit);
            }

            leftPower = power - correction;
            rightPower = power + correction;

            max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
            if (max > 1.0) {
                leftPower /= max;
                rightPower /= max;
            }

            updateDriveMotors(leftPower, rightPower, leftPower, rightPower);

            if (((loops+10) % 10) ==  0) {
                callingOpMode.telemetry.addData("gyro" , zRotation);
                callingOpMode.telemetry.addData("encoder" , getEncoderWheelYPosition());
                callingOpMode.telemetry.addData("loops", loops);
                callingOpMode.telemetry.addData("deltaD", deltaD);
                callingOpMode.telemetry.addData("deltaT", deltaT);
                callingOpMode.telemetry.addData("distance", distance);
                callingOpMode.telemetry.addData("derivative", derivative);
                callingOpMode.telemetry.update();
            }

            loops++;

            updateGlobalPosition();

            ((LinearOpMode) callingOpMode).sleep(10);
        }
    }

    public void driveStraight(double deltaY, double deltaX, float heading, double speedLimit)  throws InterruptedException {
        double error;                                           //The number of degrees between the true heading and desired heading
        double correction;                                      //Modifies power to account for error
        double max;
        double Y_P_COEFF = 0.17825353626292278;      //0.002
        double Y_I_COEFF = 0.2852056580206765;     //0.0035
        double Y_D_COEFF = 0.0003;    //0.00042
        double X_P_COEFF = 0.17825353626292278;      //0.002
        double X_I_COEFF = 0.2852056580206765;     //0.0035
        double X_D_COEFF = 0.0003;    //0.00042
        double driveY, driveX;
        double distanceY;
        double distanceX;
        long loops = 0;
        heading = (int) normalize360(heading);

        double frontLeft;
        double frontRight;
        double backLeft;
        double backRight;

        setEncoderBase();
        int lastEncoderY = 0;
        int lastEncoderX = 0;

        speedLimit = Range.clip(speedLimit, 0.0, 1.0);

        if (speedLimit==0){
            return;
        }

        double power;
        double deltaT;
        double derivativeY = 0;
        double derivativeX = 0;
        double integralY = 0;
        double integralX = 0;
        int deltaDY;
        int deltaDX;

        double y1, y2, x1, x2;
        double theta;
        double theta2;
        double gamma;
        double h;
        double posF, posS;
        double local_globalX = 0;
        double local_globalY = 0;
        double deltaPosForwards = 0;
        double deltaPosSideways = 0;
        double lastForwardPos = 0;
        double lastSidewaysPos = 0;

        double lastTime = callingOpMode.getRuntime();

        while (((((Math.abs(deltaY)-0.5) > Math.abs(local_globalY)) || ((Math.abs(deltaY)+0.5) < Math.abs(local_globalY)))  ||
                (((Math.abs(deltaX)-0.5) > Math.abs(local_globalX)) || ((Math.abs(deltaX)+0.5) < Math.abs(local_globalX)))) &&
                ((LinearOpMode) callingOpMode).opModeIsActive()) {

            error = heading - zRotation;

            while (error > 180) error = (error - 360);
            while (error <= -180) error = (error + 360);

            correction = Range.clip(error * P_DRIVE_COEFF, -1, 1);


            posF = getEncoderWheelYPosition();
            posS = getEncoderWheelXPosition();

            deltaPosForwards = (posF - lastForwardPos);
            deltaPosSideways = (posS - lastSidewaysPos);

            lastForwardPos = posF;
            lastSidewaysPos = posS;

            theta = zRotation * Math.PI / 180.0;
            y1 = deltaPosForwards * Math.cos(theta);
            y2 = -deltaPosSideways * Math.sin(theta);
            x1 = deltaPosForwards * Math.sin(theta);
            x2 = deltaPosSideways * Math.cos(theta);

            // these represent the current x,y in global coordinates ... in inches
            local_globalY += ticksToInches(y1 + y2);
            local_globalX += ticksToInches(x1 + x2);

             // these represent the current x and y translations still to do, in global coordinates ... in inches
            driveY = deltaY - local_globalY;
            driveX = deltaX - local_globalX;

            theta2 = Math.atan2(driveY, driveX);
            gamma = theta+theta2;
            h = Math.sqrt((driveX*driveX)+(driveY*driveY));

            // in robot coordinates,
            distanceY = Math.sin(gamma)*h;
            distanceX = Math.cos(gamma)*h;

            deltaT = callingOpMode.getRuntime()-lastTime;
            lastTime = callingOpMode.getRuntime();
            deltaDY = getEncoderWheelYPosition()-lastEncoderY;
            lastEncoderY = getEncoderWheelYPosition();
            deltaDX = getEncoderWheelXPosition()-lastEncoderX;
            lastEncoderX = getEncoderWheelXPosition();

            derivativeY = ((double) deltaDY)/deltaT;
            derivativeX = ((double) deltaDX)/deltaT;

            if(Math.abs(distanceY*Y_P_COEFF)<1){
                integralY += distanceY*deltaT;
            } else {
                integralY = 0;
            }
            if(Math.abs(distanceX*X_P_COEFF)<1){
                integralX += distanceX*deltaT;
            } else {
                integralX = 0;
            }

            power = (distanceY*Y_P_COEFF) + (integralY*Y_I_COEFF) - (derivativeY*Y_D_COEFF);

            frontLeft = power;
            frontRight = power;
            backLeft = power;
            backRight = power;

            power = (distanceX*X_P_COEFF) + (integralX*X_I_COEFF) - (derivativeX*X_D_COEFF);

            frontLeft += power;
            frontRight -= power;
            backLeft -= power;
            backRight += power;

            max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(frontRight)), Math.max(Math.abs(backLeft), Math.abs(backRight)));
            if (max > Math.abs(speedLimit)) {
                frontLeft /= max;
                frontRight /= max;
                backLeft /= max;
                backRight /= max;
                frontLeft *= Math.abs(speedLimit);
                frontRight *= Math.abs(speedLimit);
                backLeft *= Math.abs(speedLimit);
                backRight *= Math.abs(speedLimit);
            }

            frontLeft -= correction;
            frontRight += correction;
            backLeft -= correction;
            backRight += correction;

            max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(frontRight)), Math.max(Math.abs(backLeft), Math.abs(backRight)));
            if (max > 1.0) {
                frontLeft /= max;
                frontRight /= max;
                backLeft /= max;
                backRight /= max;
            }

            updateDriveMotors(frontLeft, frontRight, backLeft, backRight);

            if (((loops+10) % 10) ==  0) {
                callingOpMode.telemetry.addData("zRotation" , zRotation);
                callingOpMode.telemetry.addData("globalY" , local_globalY);
                callingOpMode.telemetry.addData("globalX" , local_globalX);
                callingOpMode.telemetry.addData("distanceY", distanceY);
                callingOpMode.telemetry.addData("distanceX", distanceX);
                callingOpMode.telemetry.addData("theta", theta);
                callingOpMode.telemetry.addData("theta2",theta2);
                callingOpMode.telemetry.addData("gamma",gamma);
                callingOpMode.telemetry.addData("driveY",driveY);
                callingOpMode.telemetry.addData("driveX",driveX);
                callingOpMode.telemetry.addData("loops", loops);
                callingOpMode.telemetry.update();
            }

            loops++;

            updateGlobalPosition();

            ((LinearOpMode) callingOpMode).sleep(10);
        }
    }

    /**
     * drives to a global coordinate expressed as (x, y, theta)
     * @param targetX target global x coordinate
     * @param targetY target global y coordinate
     * @param heading target global angle (theta)
     * @param speedLimit speed limit on the PID loop (0.0 < speedLimit <= 1.0)
     * @throws InterruptedException
     */
    public void driveTo(double targetX, double targetY, float heading, double speedLimit)  throws InterruptedException {
        float error = heading - zRotation;                      //The number of degrees between the true heading and desired heading
        double correction;                                      //Modifies power to account for error
        double max;
        double Y_P_COEFF = 0.17825353626292278;      //0.002
        double Y_I_COEFF = 0.2852056580206765;     //0.0035
        double Y_D_COEFF = 0.0003;    //0.00042
        double X_P_COEFF = 0.17825353626292278;      //0.002
        double X_I_COEFF = 0.2852056580206765;     //0.0035
        double X_D_COEFF = 0.0003;    //0.00042
        double T_P_COEFF = 0.0084;
        double T_I_COEFF = 0.0168;
        double T_D_COEFF = 0.0028;
        double driveY, driveX;
        double distanceY;
        double distanceX;
        long loops = 0;
        heading = (int) normalize360(heading);

        double frontLeft;
        double frontRight;
        double backLeft;
        double backRight;

        setEncoderBase();
        int lastEncoderY = 0;
        int lastEncoderX = 0;
        float lastHeading = zRotation;

        speedLimit = Range.clip(speedLimit, 0.0, 1.0);

        if (speedLimit==0){
            return;
        }

        double power;
        double deltaT;
        double derivativeY = 0;
        double derivativeX = 0;
        double derivativeT = 0;
        double integralY = 0;
        double integralX = 0;
        double integralT = 0;
        int deltaDY;
        int deltaDX;
        float deltaDT;

        double theta;
        double theta2;
        double gamma;
        double h;
        double local_globalX;
        double local_globalY;

        double lastTime = callingOpMode.getRuntime();

        while ((((((Math.abs(targetY)-1) > Math.abs(globalY)) || ((Math.abs(targetY)+1) < Math.abs(globalY)))  ||
                (((Math.abs(targetX)-1) > Math.abs(globalX)) || ((Math.abs(targetX)+1) < Math.abs(globalX))))  ||
                (Math.abs(error)>=5.0f)) &&  ((LinearOpMode) callingOpMode).opModeIsActive()) {

            error = heading - zRotation;

            while (error > 180) error = (error - 360);
            while (error <= -180) error = (error + 360);

            local_globalX = globalX;
            local_globalY = globalY;

            theta = zRotation * Math.PI / 180.0;

            // these represent the current x and y translations still to do, in global coordinates ... in inches
            driveY = targetY - local_globalY;
            driveX = targetX - local_globalX;

            theta2 = Math.atan2(driveY, driveX);
            gamma = theta+theta2;
            h = Math.sqrt((driveX*driveX)+(driveY*driveY));

            // in robot coordinates,
            distanceY = Math.sin(gamma)*h;
            distanceX = Math.cos(gamma)*h;

            deltaT = callingOpMode.getRuntime()-lastTime;
            lastTime = callingOpMode.getRuntime();
            deltaDY = getEncoderWheelYPosition()-lastEncoderY;
            lastEncoderY = getEncoderWheelYPosition();
            deltaDX = getEncoderWheelXPosition()-lastEncoderX;
            lastEncoderX = getEncoderWheelXPosition();
            deltaDT = zRotation-lastHeading;
            lastHeading = zRotation;

            derivativeY = ((double) deltaDY)/deltaT;
            derivativeX = ((double) deltaDX)/deltaT;
            derivativeT = ((double) deltaDT/deltaT);

            if(Math.abs(distanceY*Y_P_COEFF)<1 && Math.abs(distanceY)>1){
                integralY += distanceY*deltaT;
            } else {
                integralY = 0;
            }
            if(Math.abs(distanceX*X_P_COEFF)<1 && Math.abs(distanceX)>1){
                integralX += distanceX*deltaT;
            } else {
                integralX = 0;
            }
            if(Math.abs(error*T_P_COEFF)<1 && Math.abs(error)>5){
                integralT += error*deltaT;
            } else {
                integralT = 0;
            }

            power = (distanceY*Y_P_COEFF) + (integralY*Y_I_COEFF) - (derivativeY*Y_D_COEFF);

            frontLeft = power;
            frontRight = power;
            backLeft = power;
            backRight = power;

            power = (distanceX*X_P_COEFF) + (integralX*X_I_COEFF) - (derivativeX*X_D_COEFF);

            frontLeft += power;
            frontRight -= power;
            backLeft -= power;
            backRight += power;

            max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(frontRight)), Math.max(Math.abs(backLeft), Math.abs(backRight)));
            if (max > Math.abs(speedLimit)) {
                frontLeft /= max;
                frontRight /= max;
                backLeft /= max;
                backRight /= max;
                frontLeft *= Math.abs(speedLimit);
                frontRight *= Math.abs(speedLimit);
                backLeft *= Math.abs(speedLimit);
                backRight *= Math.abs(speedLimit);
            }

            correction = (error*T_P_COEFF) + (integralT*T_I_COEFF) - (derivativeT*T_D_COEFF);

            frontLeft -= correction;
            frontRight += correction;
            backLeft -= correction;
            backRight += correction;

            max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(frontRight)), Math.max(Math.abs(backLeft), Math.abs(backRight)));
            if (max > 1.0) {
                frontLeft /= max;
                frontRight /= max;
                backLeft /= max;
                backRight /= max;
            }

            updateDriveMotors(frontLeft, frontRight, backLeft, backRight);

            if (((loops+10) % 10) ==  0) {
                callingOpMode.telemetry.addData("zRotation" , zRotation);
                callingOpMode.telemetry.addData("globalY" , local_globalY);
                callingOpMode.telemetry.addData("globalX" , local_globalX);
                callingOpMode.telemetry.addData("distanceY", distanceY);
                callingOpMode.telemetry.addData("distanceX", distanceX);
                callingOpMode.telemetry.addData("theta", theta);
                callingOpMode.telemetry.addData("theta2",theta2);
                callingOpMode.telemetry.addData("gamma",gamma);
                callingOpMode.telemetry.addData("driveY",driveY);
                callingOpMode.telemetry.addData("driveX",driveX);
                callingOpMode.telemetry.addData("error", error);
                callingOpMode.telemetry.addData("loops", loops);
                callingOpMode.telemetry.update();
            }

            loops++;

            updateGlobalPosition();

            ((LinearOpMode) callingOpMode).sleep(10);
        }
    }

    public void yeetBlock() throws InterruptedException{
        double angleError, linearError;                                           //The number of degrees between the true heading and desired heading
        double correction;                                      //Modifies power to account for error
        double leftPower;                                       //Power being fed to left side of bot
        double rightPower;                                      //Power being fed to right side of bot
        double max;                                             //To be used to keep powers from exceeding 1
        double heading = zRotation;
        long loops = 0;
        double deltaTime;
        double derivativeTimeStamp = System.currentTimeMillis();
        double derivativeDistStamp = distSensor.getDistance(DistanceUnit.INCH);
        double coefP =.08;
        double coefD = -.19;
        double Dterm = 0;
        double baseP =.19;
        double power =baseP;
        double fastPower = .4;
        pos4();
        /*while(distSensor.getDistance(DistanceUnit.INCH) >= 1.6 && ((LinearOpMode) callingOpMode).opModeIsActive()) {
            updateDriveMotors(power, power, power, power);
            callingOpMode.telemetry.addData("dist:", distSensor.getDistance(DistanceUnit.INCH));
            callingOpMode.telemetry.update();
            Thread.sleep(5);
        }*/
        double distLast = distSensor.getDistance(DistanceUnit.INCH);
        double timeLast = System.currentTimeMillis();
        while(distSensor.getDistance(DistanceUnit.INCH) >= 1.6 && ((LinearOpMode) callingOpMode).opModeIsActive()) {
            angleError = heading - zRotation;
            while (angleError > 180) angleError = (angleError - 360);
            while (angleError <= -180) angleError = (angleError + 360);

            correction = Range.clip(angleError * P_DRIVE_COEFF, -1, 1);

            deltaTime = timeLast - System.currentTimeMillis();
            callingOpMode.telemetry.addData("delta time:", deltaTime);
            if(System.currentTimeMillis() - derivativeTimeStamp > 50) {
                Dterm = (derivativeDistStamp - distSensor.getDistance(DistanceUnit.INCH))/(System.currentTimeMillis() - derivativeTimeStamp);
                derivativeTimeStamp = System.currentTimeMillis();
                derivativeDistStamp = distSensor.getDistance(DistanceUnit.INCH);
            }
            power = distSensor.getDistance(DistanceUnit.INCH) * coefP + Dterm * coefD;
            callingOpMode.telemetry.addData("DDD:", Dterm + coefD);
            timeLast = System.currentTimeMillis();
            leftPower = power - correction;
            rightPower = power + correction;

            max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
            if (max > 1.0) {
                leftPower /= max;
                rightPower /= max;
            }
            updateDriveMotors(leftPower, rightPower, leftPower, rightPower);
            callingOpMode.telemetry.addData("dist:", distSensor.getDistance(DistanceUnit.INCH));
            callingOpMode.telemetry.update();
            Thread.sleep(5);
        }
        stopAndReset();
        pos5();
        pos3();
    }

    public void driveStraightTime(long millis, float heading, double power)  throws InterruptedException {
        double error;                                           //The number of degrees between the true heading and desired heading
        double correction;                                      //Modifies power to account for error
        double leftPower;                                       //Power being fed to left side of bot
        double rightPower;                                      //Power being fed to right side of bot
        double max;                                             //To be used to keep powers from exceeding 1
        long loops = 0;
        heading = (int) normalize360(heading);

        setEncoderBase();

        power = Range.clip(power, -1.0, 1.0);

        long startTime = System.currentTimeMillis();


        while ((System.currentTimeMillis() < (startTime+millis))  && ((LinearOpMode) callingOpMode).opModeIsActive()) {

            error = heading - zRotation;

            while (error > 180) error = (error - 360);
            while (error <= -180) error = (error + 360);

            correction = Range.clip(error * P_DRIVE_COEFF, -1, 1);

            leftPower = power - correction;
            rightPower = power + correction;

            max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
            if (max > 1.0) {
                leftPower /= max;
                rightPower /= max;
            }
            updateDriveMotors(leftPower, rightPower, leftPower, rightPower);

            if (((loops+10) % 10) ==  0) {
                callingOpMode.telemetry.addData("gyro" , zRotation);
                callingOpMode.telemetry.addData("encoder" , getCurrentAveragePosition());
                callingOpMode.telemetry.addData("loops", loops);
                callingOpMode.telemetry.update();
            }

            loops++;

            updateGlobalPosition();

            Thread.yield();
        }
    }

    public void turn(float turnHeading, double power) throws InterruptedException {
        int wrapFix = 0;                                        //Can be used to modify values and make math around 0 easier
        float shiftedTurnHeading = turnHeading;                 //Can be used in conjunction with wrapFix to make math around 0 easier
        long loops = 0;

        power = Math.abs(power);                                //makes sure the power is positive
        if (power>1) power = 1;                                 //makes sure the power isn't >1

        //If heading is not on correct scale, put it between 0-360
        turnHeading = normalize360(turnHeading);

        //Figure out how far the robot would have to turn in counterclockwise & clockwise directions
        float cclockwise = zRotation - turnHeading;
        float clockwise = turnHeading - zRotation;

        //Normalize cwise & ccwise values to between 0=360
        clockwise = normalize360(clockwise);
        cclockwise = normalize360(cclockwise);
        int error = 1;

        //sets the distance to the target gyro value that we will accept
        if (turnHeading - error < 0|| turnHeading + error > 360) {
            wrapFix = 180;                                      //if within the range where the clockmath breaks, shift to an easier position
            shiftedTurnHeading = normalize360(turnHeading + wrapFix);
        }

        //If it would be longer to take the ccwise path, we go *** CLOCKWISE ***
        if(Math.abs(cclockwise) >= Math.abs(clockwise)){

            updateDriveMotors(-power, power, -power, power);

            //While we're not within our error, and we haven't overshot, and the bot is running
            while(Math.abs(normalize360(zRotation + wrapFix)- shiftedTurnHeading) > error &&
                    Math.abs(cclockwise) >= Math.abs(clockwise) && ((LinearOpMode) callingOpMode).opModeIsActive()) {

                //Figure out how far the robot would have to turn in counterclockwise & clockwise directions
                cclockwise = zRotation - turnHeading;
                clockwise = turnHeading - zRotation;

                //Normalize cwise & ccwise values to between 0=360
                clockwise = normalize360(clockwise);
                cclockwise = normalize360(cclockwise);

                if ((loops % 10) ==  0) {
                    callingOpMode.telemetry.addData("gyro" , zRotation);
                    callingOpMode.telemetry.addData("loops", loops);
                    callingOpMode.telemetry.update();
                }

                loops++;

                //Chill a hot decisecond
                Thread.sleep(10);
            }
        }
        //If it would take longer to take the cwise path, we go *** COUNTERCLOCKWISE ***
        else if(Math.abs(clockwise) > Math.abs(cclockwise)) {

            updateDriveMotors(power, -power, power, -power);

            //While we're not within our error, and we haven't overshot, and the bot is running
            while (Math.abs(normalize360(zRotation + wrapFix) - shiftedTurnHeading) > error &&
                    Math.abs(clockwise) > Math.abs(cclockwise) && ((LinearOpMode) callingOpMode).opModeIsActive()) {

                //Figure out how far the robot would have to turn in counterclockwise & clockwise directions
                cclockwise = zRotation - turnHeading;
                clockwise = turnHeading - zRotation;

                //Normalize cwise & ccwise values to between 0=360
                clockwise = normalize360(clockwise);
                cclockwise = normalize360(cclockwise);

                if ((loops % 10) ==  0) {
                    callingOpMode.telemetry.addData("gyro" , zRotation);
                    callingOpMode.telemetry.addData("loops", loops);
                    callingOpMode.telemetry.update();
                }

                loops++;

                //Hold up a hot decisecond
                Thread.sleep(10);
            }
        }
    }

    public void strafe(double inches, float heading, double speedLimit)  throws InterruptedException {   //positive speedLimit == right; negative speedLimit == left

        double error;                                           //The number of degrees between the true heading and desired heading
        double correction;                                      //Modifies power to account for error
        double frontPower;                                       //Power being fed to front side of bot
        double backPower;                                      //Power being fed to back side of bot
        double max;                                             //To be used to keep powers from exceeding 1
        double P_COEFF = 0.0012;      //0.002
        double I_COEFF = 0.00192;     //0.0035
        double D_COEFF = 0.0003;    //0.00042
        if(inches<24){
            P_COEFF = 0.0015;
            I_COEFF = 0.00192;
            D_COEFF = 0.0003;
        }
        double power;
        double deltaT;
        double derivative = 0;
        double integral = 0;
        int deltaD;
        int lastEncoder;
        int distance;
        long loops = 0;
        heading = (int) normalize360(heading);

        setEncoderBase();
        lastEncoder = 0;

        int target = (int) (inches * ticksPerInchTetrix);

        speedLimit = Range.clip(speedLimit, 0.0, 1.0);

        if (speedLimit==0){
            return;
        }

        double lastTime = callingOpMode.getRuntime();

        while ((((Math.abs(target)-50) > Math.abs(getEncoderWheelXPosition()) || ((Math.abs(target)+50) < Math.abs(getEncoderWheelXPosition())))
                || (loops==0 || Math.abs(derivative)<1)) && ((LinearOpMode) callingOpMode).opModeIsActive()) {

            error = heading - zRotation;

            distance = Math.abs(target) - Math.abs(getEncoderWheelXPosition());

            while (error > 180) error = (error - 360);
            while (error <= -180) error = (error + 360);

            correction = Range.clip(error * P_DRIVE_COEFF, -1, 1);

            deltaT = callingOpMode.getRuntime()-lastTime;
            lastTime = callingOpMode.getRuntime();
            deltaD = getEncoderWheelXPosition()-lastEncoder;
            lastEncoder = getEncoderWheelXPosition();

            derivative = ((double) deltaD)/deltaT;

            if(Math.abs(distance*P_COEFF)<1){
                integral += distance*deltaT;
            } else {
                integral = 0;
            }

            power = (distance*P_COEFF) + (integral*I_COEFF) - (derivative*D_COEFF);

            if (Math.abs(power) > Math.abs(speedLimit)) {
                power /= Math.abs(power);
                power *= Math.abs(speedLimit);
            }

            frontPower = power + correction;
            backPower = power - correction;

            max = Math.max(Math.abs(frontPower), Math.abs(backPower));
            if (max > 1.0) {
                backPower /= max;
                frontPower /= max;
            }

            updateDriveMotors(-frontPower, frontPower, backPower, -backPower);

            if (((loops+10) % 10) ==  0) {
                callingOpMode.telemetry.addData("gyro" , zRotation);
                callingOpMode.telemetry.addData("encoder" , getEncoderWheelXPosition());
                callingOpMode.telemetry.addData("loops", loops);
                callingOpMode.telemetry.addData("deltaD", deltaD);
                callingOpMode.telemetry.addData("deltaT", deltaT);
                callingOpMode.telemetry.addData("distance", distance);
                callingOpMode.telemetry.addData("derivative", derivative);
                callingOpMode.telemetry.update();
            }

            loops++;

            updateGlobalPosition();

            ((LinearOpMode) callingOpMode).sleep(10);
        }
    }

    public void arcDrive (double inches, float degreesToTurn, double basePower, boolean clockwise, double p_coeff) throws InterruptedException {

        //degrees to turn  positive for clockwise

        int target = (int) (inches * ticksPerInch);
        double correction;
        double leftPower = basePower;
        double rightPower = basePower;
        double max;
        int lastEncoder;
        int currentEncoder = 0;
        int deltaEncoder;
        double proportionEncoder;
        float lastHeading;
        float currentHeading = zRotation;
        float deltaHeading;
        double proportionHeading;
        double proportionArc;
        long loops = 0;

        if(basePower<0){
            clockwise = !clockwise;
            degreesToTurn *= -1;
        }

        double P_ARC_COEFF = p_coeff;

        setEncoderBase();

        frontRight.setPower(basePower);
        backRight.setPower(basePower);
        frontLeft.setPower(basePower);
        backLeft.setPower(basePower);

        Thread.sleep(10);

        while (((LinearOpMode) callingOpMode).opModeIsActive()) {

            lastEncoder = currentEncoder;
            currentEncoder = getCurrentAveragePosition();

            deltaEncoder = currentEncoder - lastEncoder;

            lastHeading = currentHeading;
            currentHeading = zRotation;

            deltaHeading = currentHeading - lastHeading;
            deltaHeading = normalize360two(deltaHeading);

            proportionEncoder = (double) deltaEncoder / target;
            proportionHeading = (double) deltaHeading / degreesToTurn;

            proportionArc = proportionEncoder - proportionHeading;

            correction = proportionArc * P_ARC_COEFF;

            if(clockwise) {
                leftPower += correction;
                rightPower -= correction;
            } else {
                leftPower -= correction;
                rightPower += correction;
            }

            max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
            if (max > 1.0) {
                leftPower /= max;
                rightPower /= max;
            }
            frontRight.setPower(rightPower);
            backRight.setPower(rightPower);
            frontLeft.setPower(leftPower);
            backLeft.setPower(leftPower);

            if (((loops + 10) % 10) == 0) {
                callingOpMode.telemetry.addData("TARGET", target);
                callingOpMode.telemetry.addData("gyro", zRotation);
                callingOpMode.telemetry.addData("encoder", currentEncoder);
                callingOpMode.telemetry.addData("heading", currentHeading);
                callingOpMode.telemetry.addData("dEncoder", deltaEncoder);
                callingOpMode.telemetry.addData("dHeading", deltaHeading);
                callingOpMode.telemetry.addData("pEncoder", proportionEncoder);
                callingOpMode.telemetry.addData("pHeading", proportionHeading);
                callingOpMode.telemetry.addData("arc", proportionArc);
                callingOpMode.telemetry.addData("correction", correction);
                callingOpMode.telemetry.addData("leftPower", leftPower);
                callingOpMode.telemetry.addData("rightPower", rightPower);
                callingOpMode.telemetry.addData("loops", loops);
                callingOpMode.telemetry.update();
            }

            loops++;

            updateGlobalPosition();

            Thread.sleep(10);

            if(Math.abs(target) <= Math.abs(getCurrentAveragePosition())) {
                callingOpMode.telemetry.addData("TARGET", target);
                callingOpMode.telemetry.addData("gyro", zRotation);
                callingOpMode.telemetry.addData("encoder", currentEncoder);
                callingOpMode.telemetry.addData("heading", currentHeading);
                callingOpMode.telemetry.addData("dEncoder", deltaEncoder);
                callingOpMode.telemetry.addData("dHeading", deltaHeading);
                callingOpMode.telemetry.addData("pEncoder", proportionEncoder);
                callingOpMode.telemetry.addData("pHeading", proportionHeading);
                callingOpMode.telemetry.addData("arc", proportionArc);
                callingOpMode.telemetry.addData("correction", correction);
                callingOpMode.telemetry.addData("leftPower", leftPower);
                callingOpMode.telemetry.addData("rightPower", rightPower);
                callingOpMode.telemetry.addData("loops", loops);
                callingOpMode.telemetry.update();
                break;
            }
        }
    }

    public void grabFoundation () throws InterruptedException {
        foundRight.setPosition(0.75);
        foundLeft.setPosition(0);
        Thread.sleep(1100);
    }

    public void releaseFoundation () throws InterruptedException {
        foundRight.setPosition(0);
        foundLeft.setPosition(0.75);
    }

    public void mainFlopDown () throws InterruptedException {
        mainFlop.setPosition(0);
        Thread.sleep(400);
    }

    public void mainFlopMid () throws InterruptedException {
        mainFlop.setPosition(0.15);
        Thread.sleep(400);
    }

    public void mainFlopUp () throws InterruptedException {
        mainFlop.setPosition(0.7);
        subFlop.setPosition(0.35);
        Thread.sleep(400);
    }

    public void grabStone () throws InterruptedException {
        subFlop.setPosition(0.35);
        Thread.sleep(400);
    }

    public void releaseStone () throws InterruptedException {
        subFlop.setPosition(0.7);
        Thread.sleep(400);
    }



    /*
    *   pos 1 == closed & up
    *   pos 2 == open & mid
    *   pos 3 == closed & mid
    *   pos 4 == open & down
    *   pos 5 == closed & down
     */

    public void pos1 () throws InterruptedException {
        pos = 1;
        mainFlop.setPosition(0.7);
        subFlop.setPosition(0.35);
        Thread.sleep(400);
    }

    public void pos2 () throws InterruptedException {
        pos = 2;
        mainFlop.setPosition(0.15);
        subFlop.setPosition(0.7);
        Thread.sleep(400);
    }

    public void pos3 () throws InterruptedException {
        pos = 3;
        mainFlop.setPosition(0.15);
        subFlop.setPosition(0.30);
        Thread.sleep(400);
    }

    public void pos4 () throws InterruptedException {
        pos = 4;
        mainFlop.setPosition(0);
        subFlop.setPosition(0.7);
        Thread.sleep(400);
    }

    public void pos5 () throws InterruptedException {
        pos = 5;
        mainFlop.setPosition(0);
        subFlop.setPosition(0.30);
        Thread.sleep(400);
    }

    public int getPos () {
        return pos;
    }



    public void lift3F () throws InterruptedException {
        if (lift.getCurrentPosition() < 1000) {
            lift.setPower(0.9);
            while (lift.getCurrentPosition() < 1000) {Thread.sleep(10);}
        } else if (lift.getCurrentPosition() > 1000) {
            lift.setPower(-0.9);
            while (lift.getCurrentPosition() > 1000) {Thread.sleep(10);}
        }
        lift.setPower(0);
    }

    public void lift2F () throws InterruptedException {
        if (lift.getCurrentPosition() < 500) {
            lift.setPower(0.9);
            while (lift.getCurrentPosition() < 500) {Thread.sleep(10);}
        } else if (lift.getCurrentPosition() > 500) {
            lift.setPower(-0.9);
            while (lift.getCurrentPosition() > 500) {Thread.sleep(10);}
        }
        lift.setPower(0);
    }

    public void lift1F () throws InterruptedException {
        if (lift.getCurrentPosition() < 100) {
            lift.setPower(0.9);
            while (lift.getCurrentPosition() < 100) {Thread.sleep(10);}
        } else if (lift.getCurrentPosition() > 100) {
            lift.setPower(-0.9);
            while (lift.getCurrentPosition() > 100) {Thread.sleep(10);}
        }
        lift.setPower(0);
    }

    public void liftReset () throws InterruptedException {
        if (lift.getCurrentPosition() < 10) {
            lift.setPower(0.9);
            while (lift.getCurrentPosition() < 10) {Thread.sleep(10);}
        } else if (lift.getCurrentPosition() > 10) {
            lift.setPower(-0.9);
            while (lift.getCurrentPosition() > 10) {Thread.sleep(10);}
        }
        lift.setPower(0);
    }

    protected float normalize360two(float val) {
        while (val > 180f || val < -180f) {

            if (val > 180f) {
                val -= 360f;
            }

            if (val < -180f) {
                val += 360f;
            }
        }
        return val;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientation);

        sensorDataCounter++;

        rawGyro = (float) Math.toDegrees(orientation[0]);

        //If the zero hasn't been zeroed do the zero
        if (!hasBeenZeroed) {
            hasBeenZeroed = true;
            zero = rawGyro;
        }
        //Normalize zRotation to be used
        zRotation = normalize360(rawGyro - zero);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    protected float normalize360(float val) {
        while (val > 360 || val < 0) {

            if (val > 360) {
                val -= 360;
            }

            if (val < 0) {
                val += 360;
            }
        }
        return val;
    }

    public void deconstruct(){
        mSensorManager.unregisterListener(this);
    }
}