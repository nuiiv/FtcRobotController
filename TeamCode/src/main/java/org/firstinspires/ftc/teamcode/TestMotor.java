package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Tests each motor on its own, to check wiring, ports and configuration names.
 * Uses the same names as RobotHardware. A motor that is missing from the configuration is
 * reported on telemetry instead of crashing, so you can test whichever motors you have.
 *
 * Gamepad 1 (hold a button to spin that motor at 50% forward):
 *   X  left_drive (port 0)     Y  intake  (port 2)
 *   A  right_drive (port 1)    B  exhaust (port 3)
 *   left stick Y               all configured motors together
 *
 * Servos:
 *   left bumper   left_cont  (port 0) spins while held
 *   right bumper  right_cont (port 2) spins while held
 *   D-pad up/down windmill_servo (port 4) spins forward / backward while held
 */
@TeleOp(name = "Test Motor", group = "Test")
public class TestMotor extends LinearOpMode {

    static final double TEST_POWER = 0.5;

    static final String[] NAMES  = {"left_drive",      "right_drive",     "intake",          "exhaust"};
    static final String[] LABELS = {"Left drive  (X)", "Right drive (A)", "Intake      (Y)", "Exhaust     (B)"};

    @Override
    public void runOpMode() {
        DcMotor[] motors = new DcMotor[NAMES.length];
        for (int i = 0; i < NAMES.length; i++) {
            motors[i] = hardwareMap.tryGet(DcMotor.class, NAMES[i]);  // null if not in the configuration
            if (motors[i] != null) {
                motors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                motors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                motors[i].setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
        }

        CRServo leftCont  = hardwareMap.tryGet(CRServo.class, "left_cont");
        CRServo rightCont = hardwareMap.tryGet(CRServo.class, "right_cont");
        CRServo windmill  = hardwareMap.tryGet(CRServo.class, "windmill_servo");

        telemetry.addData("Status", "Ready - press START");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            boolean[] buttons = {gamepad1.x, gamepad1.a, gamepad1.y, gamepad1.b};
            double stick = -gamepad1.left_stick_y;  // pushing the stick forward gives a negative value

            for (int i = 0; i < motors.length; i++) {
                if (motors[i] == null) {
                    telemetry.addData(LABELS[i], "NOT CONFIGURED - add \"%s\"", NAMES[i]);
                    continue;
                }
                double power = buttons[i] ? TEST_POWER : stick;
                motors[i].setPower(power);
                telemetry.addData(LABELS[i], "power %5.2f   encoder %d", power, motors[i].getCurrentPosition());
            }

            // ---- Servos ----
            if (leftCont == null) {
                telemetry.addData("Left cont   (LB)", "NOT CONFIGURED - add \"left_cont\" as Continuous Rotation Servo");
            } else {
                leftCont.setPower(gamepad1.left_bumper ? 1.0 : 0.0);
                telemetry.addData("Left cont   (LB)", gamepad1.left_bumper ? "spinning" : "off");
            }
            if (rightCont == null) {
                telemetry.addData("Right cont  (RB)", "NOT CONFIGURED - add \"right_cont\" as Continuous Rotation Servo");
            } else {
                rightCont.setPower(gamepad1.right_bumper ? 1.0 : 0.0);
                telemetry.addData("Right cont  (RB)", gamepad1.right_bumper ? "spinning" : "off");
            }
            if (windmill == null) {
                telemetry.addData("Windmill (D-pad)", "NOT CONFIGURED - add \"windmill_servo\" as Continuous Rotation Servo");
            } else {
                double windmillPower = gamepad1.dpad_up ? 1.0 : gamepad1.dpad_down ? -1.0 : 0.0;
                windmill.setPower(windmillPower);
                telemetry.addData("Windmill (D-pad)", windmillPower > 0 ? "forward" : windmillPower < 0 ? "backward" : "off");
            }

            telemetry.update();
        }

        for (DcMotor m : motors) {
            if (m != null) m.setPower(0);
        }
        if (leftCont != null)  leftCont.setPower(0);
        if (rightCont != null) rightCont.setPower(0);
        if (windmill != null)  windmill.setPower(0);
    }
}
