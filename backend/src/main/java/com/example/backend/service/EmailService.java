package com.example.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendEmailVerification(String to, String username, String token) throws MessagingException {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;

        String htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: #FF8C42; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f9f9f9; padding: 30px; border: 1px solid #eee; }
                .button { display: inline-block; background: #FF8C42; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Verify Your Email</h1>
                </div>
                <div class="content">
                    <p>Hi %s,</p>
                    <p>Thank you for registering with Camera Shop! Please verify your email address by clicking the button below:</p>
                    <p style="text-align: center;">
                        <a href="%s" class="button">Verify Email</a>
                    </p>
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #FF8C42;">%s</p>
                    <p>This link will expire in 24 hours.</p>
                    <p>If you didn't create an account, you can safely ignore this email.</p>
                </div>
                <div class="footer">
                    <p>&copy; 2024 Camera Shop. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(username, verificationLink, verificationLink);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Verify Your Email - Camera Shop");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendOrderConfirmation(String to, String username, String orderCode,
                                      String orderDetails, double totalAmount,
                                      String paymentStatus) throws MessagingException {

        String htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: #FF8C42; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f9f9f9; padding: 30px; border: 1px solid #eee; }
                .order-details { background: white; padding: 15px; margin: 15px 0; border-radius: 5px; }
                .total { font-size: 18px; font-weight: bold; color: #FF8C42; }
                .status { display: inline-block; padding: 5px 15px; border-radius: 20px; font-size: 14px; margin-top: 10px; }
                .status-success { background: #d4edda; color: #155724; }
                .status-pending { background: #fff3cd; color: #856404; }
                .status-failed { background: #f8d7da; color: #721c24; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Order Confirmation</h1>
                </div>
                <div class="content">
                    <p>Hi %s,</p>
                    <p>Thank you for your order! Your order has been confirmed.</p>
                    <p><strong>Order Code:</strong> %s</p>
                    <span class="status status-%s">%s</span>

                    <div class="order-details">
                        <h3>Order Details:</h3>
                        %s
                    </div>

                    <p class="total">Total: ₫%s</p>

                    <p>We'll send you another email when your order ships.</p>
                </div>
                <div class="footer">
                    <p>&copy; 2024 Camera Shop. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(username, orderCode,
                      paymentStatus.toLowerCase().equals("success") ? "success" : "pending",
                      paymentStatus, orderDetails, String.format("%,0f", totalAmount));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Order Confirmation - Camera Shop");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendRentalConfirmation(String to, String username, String rentalCode,
                                       String assetName, String startDate, String endDate,
                                       double totalRentFee, double depositFee) throws MessagingException {

        String htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: #FF8C42; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f9f9f9; padding: 30px; border: 1px solid #eee; }
                .rental-details { background: white; padding: 15px; margin: 15px 0; border-radius: 5px; }
                .total { font-size: 18px; font-weight: bold; color: #FF8C42; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Rental Confirmation</h1>
                </div>
                <div class="content">
                    <p>Hi %s,</p>
                    <p>Your rental has been confirmed!</p>
                    <p><strong>Rental Code:</strong> %s</p>

                    <div class="rental-details">
                        <h3>Rental Details:</h3>
                        <p><strong>Equipment:</strong> %s</p>
                        <p><strong>Rental Period:</strong> %s to %s</p>
                        <p><strong>Rental Fee:</strong> ₫%s</p>
                        <p><strong>Deposit:</strong> ₫%s</p>
                    </div>

                    <p>Please bring a valid ID when picking up your equipment.</p>
                </div>
                <div class="footer">
                    <p>&copy; 2024 Camera Shop. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(username, rentalCode, assetName, startDate, endDate,
                      String.format("%,0f", totalRentFee), String.format("%,0f", depositFee));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Rental Confirmation - Camera Shop");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
