package ru.driftcars;

import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DriftCar {
    private final ArmorStand carEntity;
    private final Player driver;
    private Vector velocity = new Vector(0, 0, 0);
    private float yaw;

    private final double maxSpeed = 1.3;
    private final double acceleration = 0.06;
    private final double baseGrip = 0.12;
    private final double driftGrip = 0.025;
    private final float turnSpeed = 5.0f;

    public DriftCar(ArmorStand carEntity, Player driver) {
        this.carEntity = carEntity;
        this.driver = driver;
        this.yaw = driver.getLocation().getYaw();
    }

    public void tick() {
        if (!carEntity.isValid() || !driver.isOnline() || !carEntity.getPassengers().contains(driver)) {
            return;
        }

        Input input = driver.getCurrentInput();
        boolean forward = input.isForward();
        boolean backward = input.isBackward();
        boolean left = input.isLeft();
        boolean right = input.isRight();
        boolean handbrake = input.isJump();

        Location loc = carEntity.getLocation();

        if (velocity.length() > 0.05) {
            float currentTurn = handbrake ? turnSpeed * 1.45f : turnSpeed;
            if (left) yaw -= currentTurn;
            if (right) yaw += currentTurn;
        }

        Vector forwardDir = new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw))).normalize();

        if (forward) {
            velocity.add(forwardDir.clone().multiply(acceleration));
        } else if (backward) {
            velocity.subtract(forwardDir.clone().multiply(acceleration * 0.6));
        }

        double speed = velocity.length();
        if (speed > 0.01) {
            double grip = handbrake ? driftGrip : baseGrip;
            Vector targetVelocity = forwardDir.clone().multiply(speed);
            velocity = velocity.multiply(1.0 - grip).add(targetVelocity.multiply(grip));
        }

        velocity.multiply(0.96);
        if (velocity.length() > maxSpeed) {
            velocity.normalize().multiply(maxSpeed);
        }

        if (speed > 0.35 && velocity.clone().normalize().angle(forwardDir) > 0.3) {
            loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 0.2, 0), 2, 0.2, 0, 0.2, 0.01);
        }

        Vector finalVelocity = velocity.clone().setY(carEntity.getVelocity().getY());
        carEntity.setVelocity(finalVelocity);
        carEntity.setRotation(yaw, 0);
    }
}
