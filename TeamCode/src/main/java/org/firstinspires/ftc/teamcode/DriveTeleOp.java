package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Driver-controlled tank drive with intake and exhaust.
 *
 * Gamepad 1:
 *   left stick Y    drive forward / back
 *   right stick X   turn
 *   left bumper     hold for slow mode (precise lining up)
 *   right trigger   tap to turn intake + gathering servos ON / OFF (pull balls in)
 *   left trigger    hold to reverse intake + gathering servos (unjam); goes back to before when released
 *   right bumper    tap to turn exhaust ON / OFF (push balls out)
 *   B (PS5: Circle) hold to reverse exhaust; goes back to before when released
 *   D-pad right     hold to spin windmill forward
 *   D-pad left      hold to spin windmill backward
 */
@TeleOp(name = "Drive TeleOp", group = "Competition")
public class DriveTeleOp extends LinearOpMode {

    static final double SLOW_SCALE = 0.35;
    static final double TRIGGER_THRESHOLD = 0.2;

    @Override
    public void runOpMode() {
        RobotHardware robot = new RobotHardware(this);
        robot.init();

        telemetry.addData("Status", "Ready - press START");
        telemetry.update();
        waitForStart();

        boolean intakeOn = false, exhaustOn = false;
        boolean lastRightTrigger = false, lastRightBumper = false;

        while (opModeIsActive()) {
            // ---- Drive ----
            double forward = -gamepad1.left_stick_y;  // pushing the stick forward gives a negative value
            double turn    =  gamepad1.right_stick_x;
            if (gamepad1.left_bumper) {
                forward *= SLOW_SCALE;
                turn    *= SLOW_SCALE;
            }
            robot.drive(forward, turn);

            // ---- Intake ----
            // Toggle only on the moment the trigger is pressed, not every loop while it's held.
            boolean rightTrigger = gamepad1.right_trigger > TRIGGER_THRESHOLD;
            if (rightTrigger && !lastRightTrigger) intakeOn = !intakeOn;
            lastRightTrigger = rightTrigger;

            double intake = intakeOn ? 1 : 0;
            if (gamepad1.left_trigger > TRIGGER_THRESHOLD) intake = -1;  // unjam overrides while held
            robot.setIntake(intake);

            // ---- Exhaust ----
            if (gamepad1.right_bumper && !lastRightBumper) exhaustOn = !exhaustOn;
            lastRightBumper = gamepad1.right_bumper;

            double exhaust = exhaustOn ? 1 : 0;
            if (gamepad1.b) exhaust = -1;  // reverse overrides while held
            robot.setExhaust(exhaust);

            // ---- Windmill ----
            double windmill = 0;
            if (gamepad1.dpad_right)     windmill = 1;
            else if (gamepad1.dpad_left) windmill = -1;
            robot.setWindmill(windmill);

            telemetry.addData("Drive", "fwd %.2f  turn %.2f%s", forward, turn, gamepad1.left_bumper ? "  (SLOW)" : "");
            telemetry.addData("Intake",  intake  > 0 ? "IN"  : intake  < 0 ? "REVERSE" : "off");
            telemetry.addData("Exhaust", exhaust > 0 ? "OUT" : exhaust < 0 ? "REVERSE" : "off");
            telemetry.addData("Windmill", windmill > 0 ? "FORWARD" : windmill < 0 ? "BACKWARD" : "off");
            telemetry.update();
        }

        robot.stopAll();
    }
}
