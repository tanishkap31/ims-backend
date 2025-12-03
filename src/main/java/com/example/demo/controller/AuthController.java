package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;  // Add this import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.mail.MessagingException;  // Add this import
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;


@RestController
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserRepository repo;

    @Autowired
    private EmailService emailService;  // Inject EmailService

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
 // ----------------- TEST EMAIL HERE -----------------
    @GetMapping("/test-email")
    public String testEmail() throws MessagingException {
        emailService.sendHtmlEmail("patiltani2005@gmail.com", "Test Mail", "<h1>Hello Testing</h1>");
        return "Email Sent Successfully!";}

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (repo.findByEmail(user.getEmail()) != null) {
            return "Email already exists";
        }
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
        return "Registration Successful";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        User user = repo.findByEmail(body.get("email"));

        Map<String, String> response = new HashMap<>();

        if (user == null) {
            response.put("message", "Invalid Email");
            return response;
        }

        if (!encoder.matches(body.get("password"), user.getPassword())) {
            response.put("message", "Invalid Password");
            return response;
        }

        response.put("message", "Login Successful");
        response.put("role", user.getRole());  // 🟢 important for redirect

        return response;
    }


    @PostMapping("/forgot")
    public String forgot(@RequestBody Map<String, String> body) {

        User user = repo.findByEmail(body.get("email"));
        if (user == null) {
            return "Email not registered";
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        repo.save(user);

        String resetLink = "http://localhost:8080/reset-password.html?token=" + token;

        String htmlBody = """
            <html>
            <body style="font-family:Arial, sans-serif; background-color:#f4f4f4; padding:20px;">
            <div style="max-width:600px; margin:auto; background-color:white; padding:20px; border-radius:10px; box-shadow:0 0 10px rgba(0,0,0,0.1);">
                <h2 style="color:#0073e6;">Reset Your Password</h2>
                <p>Hello,</p>
                <p>We received a request to reset your password. Click the button below to reset it:</p>
                <a href="%s" style="background-color:#0078ff; color:white; padding:10px 20px; text-decoration:none; border-radius:5px; display:inline-block;">Reset Password</a>
                <p>If you didn't request this, ignore this email.</p>
                <p>This link expires in 15 minutes.</p>
                <p>Best,<br>Your App Team</p>
            </div>
            </body>
            </html>
            """.formatted(resetLink);

        try {
            emailService.sendHtmlEmail(user.getEmail(), "Password Reset Request", htmlBody);
            return "Password reset link sent to your email!";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error sending email";
        }
    }

    @PostMapping("/reset")
    public String reset(@RequestBody Map<String, String> body) {
        User user = repo.findByResetToken(body.get("token"));
        if (user == null) return "Invalid Token";

        user.setPassword(encoder.encode(body.get("newPassword")));
        user.setResetToken(null);
        repo.save(user);

        return "Password Reset Successful";
    }
}
