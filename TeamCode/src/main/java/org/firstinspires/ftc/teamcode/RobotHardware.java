package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Shared hardware for all of our OpModes: a 2-motor tank drive (one motor per side),
 * a ball intake with 2 gathering servos, an exhaust, and the windmill servo.
 *
 * Robot configuration (Driver Station -> Configure Robot -> Control Hub):
 *   Motors
 *     port 0  left_drive      both left wheels
 *     port 1  right_drive     both right wheels
 *     port 2  intake          pulls balls in
 *     port 3  exhaust         pushes balls out
 *   Servos
 *     port 0  left_cont       Continuous Rotation Servo, gathers balls (runs with the intake)
 *     port 2  right_cont      Continuous Rotation Servo, gathers balls (runs with the intake)
 *     port 4  windmill_servo  Continuous Rotation Servo, spins the windmill
 *
 * Before the first test, set the constants below to match your motors and wheels.
 */
public class RobotHardware {

    // ---- Drivetrain constants: CHANGE THESE to match your robot ----
    // Default: goBILDA 5203 Yellow Jacket 312 RPM (537.7 ticks/rev) with 104 mm wheels.
    static final double COUNTS_PER_MOTOR_REV  = 537.7;
    static final double DRIVE_GEAR_REDUCTION  = 1.0;   // > 1.0 if geared down after the motor
    static final double WHEEL_DIAMETER_INCHES = 104.0 / 25.4;
    static final double COUNTS_PER_INCH =
            (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_INCHES * Math.PI);

    // Mechanism speeds (0 to 1). Turn these down if balls jam or fly out too hard.
    static final double INTAKE_POWER  = 1.0;
    static final double EXHAUST_POWER = 1.0;
    static final double GATHER_POWER  = 1.0;   // continuous servos, 0 to 1

    static final double WINDMILL_POWER = 1.0;  // continuous servo, 0 to 1

    public DcMotor leftDrive, rightDrive, intake, exhaust;
    public CRServo leftGather, rightGather, windmill;

    private final LinearOpMode opMode;

    public RobotHardware(LinearOpMode opMode) {
        this.opMode = opMode;
    }

    public void init() {
        HardwareMap hw = opMode.hardwareMap;

        leftDrive  = hw.get(DcMotor.class, "left_drive");
        rightDrive = hw.get(DcMotor.class, "right_drive");
        intake     = hw.get(DcMotor.class, "intake");
        exhaust    = hw.get(DcMotor.class, "exhaust");

        // Test these on the robot: push the left stick forward and both sides should roll forward.
        // Flip FORWARD <-> REVERSE on a side that spins backwards, or on a mechanism that runs the wrong way.
        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);
        intake.setDirection(DcMotor.Direction.FORWARD);
        exhaust.setDirection(DcMotor.Direction.FORWARD);

        for (DcMotor m : new DcMotor[] {leftDrive, rightDrive}) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        // Mechanisms coast to a stop and don't need encoders.
        for (DcMotor m : new DcMotor[] {intake, exhaust}) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        leftGather  = hw.get(CRServo.class, "left_cont");
        rightGather = hw.get(CRServo.class, "right_cont");
        windmill    = hw.get(CRServo.class, "windmill_servo");

        // The gathering servos face each other, so one is reversed to make both pull balls inward.
        // If a side pushes balls out instead, flip its direction.
        leftGather.setDirection(DcMotorSimple.Direction.FORWARD);
        rightGather.setDirection(DcMotorSimple.Direction.REVERSE);
        windmill.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    /**
     * Arcade drive.
     * @param forward forward (+) / backward (-)
     * @param turn    turn right (+) / left (-)
     */
    public void drive(double forward, double turn) {
        double left  = forward + turn;
        double right = forward - turn;

        // Scale down so neither side goes over 100% while keeping the direction of motion.
        double max = Math.max(1.0, Math.max(Math.abs(left), Math.abs(right)));
        leftDrive.setPower(left / max);
        rightDrive.setPower(right / max);
    }

    public void stopDrive() {
        drive(0, 0);
    }

    /** Runs the intake motor and both gathering servos together. +1 = pull balls in, -1 = spit them out (unjam), 0 = off. */
    public void setIntake(double direction) {
        intake.setPower(direction * INTAKE_POWER);
        leftGather.setPower(direction * GATHER_POWER);
        rightGather.setPower(direction * GATHER_POWER);
    }

    /** +1 = spin the windmill forward, -1 = backward, 0 = off. */
    public void setWindmill(double direction) {
        windmill.setPower(direction * WINDMILL_POWER);
    }

    /** +1 = push balls out, -1 = reverse, 0 = off. */
    public void setExhaust(double direction) {
        exhaust.setPower(direction * EXHAUST_POWER);
    }

    public void stopAll() {
        stopDrive();
        setIntake(0);
        setExhaust(0);
        setWindmill(0);
    }

    /**
     * Drive straight a set distance using the encoders. Blocks until done, timed out, or the OpMode stops.
     * @param inches + forward, - backward
     */
    public void driveInches(double inches, double speed, double timeoutSec) {
        moveSides(inches, inches, speed, timeoutSec);
    }

    /**
     * Spin in place by running the sides in opposite directions.
     * @param sideInches how far each side travels; + turns right, - turns left.
     *                   Tune on the field to get the angle you want.
     */
    public void turnInches(double sideInches, double speed, double timeoutSec) {
        moveSides(sideInches, -sideInches, speed, timeoutSec);
    }

    private void moveSides(double leftInches, double rightInches, double speed, double timeoutSec) {
        leftDrive.setTargetPosition(leftDrive.getCurrentPosition()   + (int) Math.round(leftInches * COUNTS_PER_INCH));
        rightDrive.setTargetPosition(rightDrive.getCurrentPosition() + (int) Math.round(rightInches * COUNTS_PER_INCH));

        for (DcMotor m : new DcMotor[] {leftDrive, rightDrive}) {
            m.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            m.setPower(Math.abs(speed));
        }

        ElapsedTime timer = new ElapsedTime();
        while (opMode.opModeIsActive() && timer.seconds() < timeoutSec
                && (leftDrive.isBusy() || rightDrive.isBusy())) {
            opMode.telemetry.addData("Driving", "left %.1f in, right %.1f in", leftInches, rightInches);
            opMode.telemetry.addData("Encoders L / R", "%d / %d",
                    leftDrive.getCurrentPosition(), rightDrive.getCurrentPosition());
            opMode.telemetry.update();
        }

        stopDrive();
        for (DcMotor m : new DcMotor[] {leftDrive, rightDrive}) {
            m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }
}
