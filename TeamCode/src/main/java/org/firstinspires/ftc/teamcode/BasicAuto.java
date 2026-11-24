package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * Starter autonomous: waits an optional delay, then drives with the encoders and parks.
 * Replace the steps in runOpMode() with this season's scoring routine.
 *
 * preselectTeleOp makes the Driver Station queue up our TeleOp automatically when Auto ends.
 */
@Autonomous(name = "Basic Auto", group = "Competition", preselectTeleOp = "Drive TeleOp")
public class BasicAuto extends LinearOpMode {

    static final double DRIVE_SPEED = 0.5;

    @Override
    public void runOpMode() {
        RobotHardware robot = new RobotHardware(this);
        robot.init();

        // Start delay, chosen during INIT with the D-pad (lets you stay out of a partner's way).
        double startDelaySec = 0;
        boolean lastUp = false, lastDown = false;

        while (opModeInInit()) {
            if (gamepad1.dpad_up && !lastUp)     startDelaySec = Math.min(startDelaySec + 1, 15);
            if (gamepad1.dpad_down && !lastDown) startDelaySec = Math.max(startDelaySec - 1, 0);
            lastUp = gamepad1.dpad_up;
            lastDown = gamepad1.dpad_down;

            telemetry.addData("Start delay", "%.0f s  (D-pad up/down)", startDelaySec);
            telemetry.addData("Status", "Ready - press START");
            telemetry.update();
        }
        if (isStopRequested()) return;

        sleep((long) (startDelaySec * 1000));

        // ---- Autonomous steps: edit these ----
        robot.driveInches(24, DRIVE_SPEED, 4.0);   // forward 24 in to park

        // Example of scoring with the exhaust: run it for 2 seconds, then stop.
        // robot.setExhaust(1);
        // sleep(2000);
        // robot.setExhaust(0);

        robot.stopAll();
        telemetry.addData("Status", "Auto complete");
        telemetry.update();
    }
}
