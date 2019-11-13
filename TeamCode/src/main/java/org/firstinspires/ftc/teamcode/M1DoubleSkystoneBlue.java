package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous (name="M1DoubleSkystoneBlue")
public class M1DoubleSkystoneBlue extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        int cameraPos;

        RobotBaseM1 robotBase = new RobotBaseM1(this);

        waitForStart();

        cameraPos = 0;

        robotBase.driveStraight(4.5, 0, -0.9);
        robotBase.driveStraight(4.5, 0, -0.4);
        robotBase.turn(68, 0.6);
        robotBase.stopAndReset();
        switch(cameraPos) {
            case 0:
                robotBase.driveStraight(3, 90, 0.5);
                robotBase.driveStraight(4, 90, 0.3);
                break;
            case 1:
                robotBase.driveStraight(3, 90, 0.7);
                break;
            case 2:
                break;
        }
        robotBase.turn(158, 0.6);
        robotBase.stopAndReset();
        robotBase.pos4();
        robotBase.driveStraight(2, 180, 0.2);
        robotBase.driveStraight(10, 180, 0.2);
        robotBase.stopAndReset();
        robotBase.pos5();
        robotBase.pos3();
        robotBase.driveStraight(4, 180, -0.3);
        robotBase.turn(112, 0.6);
        robotBase.stopAndReset();
        switch(cameraPos) {
            case 0:
                robotBase.driveStraight(28, 90, 0.6);
                robotBase.driveStraight(38.5, 90, 0.2);
                break;
            case 1:
                robotBase.driveStraight(44, 90);
                robotBase.driveStraight(16, 90, 0.2);
                break;
            case 2:
                break;
        }
        robotBase.turn(158, 0.6);
        robotBase.stopAndReset();
        robotBase.lift1F();
        robotBase.driveStraight(4, 180, 0.3);
        robotBase.stopAndReset();
        robotBase.pos5();
        robotBase.pos4();
        robotBase.driveStraight(4, 180, -0.3);
        robotBase.turn(112, 0.6);
        robotBase.stopAndReset();
        robotBase.liftReset();
        switch(cameraPos) {
            case 0:
                robotBase.driveStraight(28, 95, -0.6);
                robotBase.driveStraight(64, 90, -0.2);
                break;
            case 1:
                robotBase.driveStraight(68, 90, -0.9);
                robotBase.driveStraight(15, 90, -0.2);
                break;
            case 2:
                break;
        }
        robotBase.turn(158, 0.6);
        robotBase.stopAndReset();
        robotBase.driveStraight(4, 180, 0.2);
        robotBase.stopAndReset();
        robotBase.pos5();
        robotBase.pos3();
        robotBase.driveStraight(4, 180, -0.3);
        robotBase.turn(112, 0.6);
        robotBase.stopAndReset();
        switch(cameraPos) {
            case 0:
                robotBase.driveStraight(65, 90);
                robotBase.driveStraight(12, 90, 0.2);
                break;
            case 1:
                robotBase.driveStraight(68, 90);
                robotBase.driveStraight(15, 90, 0.2);
                break;
            case 2:
                break;
        }
        robotBase.turn(158, 0.6);
        robotBase.stopAndReset();
        robotBase.lift2F();
        robotBase.driveStraight(4, 180, 0.3);
        robotBase.stopAndReset();
        robotBase.pos5();
        robotBase.pos4();
        robotBase.driveStraight(4, 180, -0.3);
        robotBase.turn(112, 0.6);
        robotBase.stopAndReset();
        robotBase.pos1();
        robotBase.liftReset();
        robotBase.driveStraight(32, 90, -0.9);
        robotBase.stopAndReset();
    }
}
