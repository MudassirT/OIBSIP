const nodemailer = require('nodemailer');
const config = require('../config/env');

let transporter = null;
let etherealAccount = null;

const getTransporter = async () => {
  if (transporter) return transporter;

  // 1. Check if custom SMTP is configured
  if (config.email.host && config.email.user && config.email.pass) {
    try {
      transporter = nodemailer.createTransport({
        host: config.email.host,
        port: config.email.port,
        secure: config.email.port === 465,
        auth: {
          user: config.email.user,
          pass: config.email.pass
        }
      });
      console.log(`Email Transporter initialized with SMTP host: ${config.email.host}`);
      return transporter;
    } catch (err) {
      console.warn(`Failed to initialize custom SMTP: ${err.message}. Falling back to Ethereal/Console.`);
    }
  }

  // 2. Fall back to Ethereal test account in development
  try {
    if (!etherealAccount) {
      etherealAccount = await nodemailer.createTestAccount();
      console.log(`Generated Ethereal Test Email account: ${etherealAccount.user}`);
    }
    transporter = nodemailer.createTransport({
      host: 'smtp.ethereal.email',
      port: 587,
      secure: false,
      auth: {
        user: etherealAccount.user,
        pass: etherealAccount.pass
      }
    });
    return transporter;
  } catch (err) {
    console.warn(`Could not create Ethereal account (${err.message}). Emails will be logged to console.`);
    return null;
  }
};

/**
 * Universal mail sender with graceful console fallback
 */
const sendMail = async ({ to, subject, html, text }) => {
  try {
    const activeTransporter = await getTransporter();
    if (activeTransporter) {
      const info = await activeTransporter.sendMail({
        from: config.email.from,
        to,
        subject,
        text: text || html.replace(/<[^>]*>?/gm, ''),
        html
      });

      const previewUrl = nodemailer.getTestMessageUrl(info);
      console.log(`\n================= EMAIL SENT =================`);
      console.log(`To: ${to}`);
      console.log(`Subject: ${subject}`);
      if (previewUrl) {
        console.log(`Preview URL (Ethereal): ${previewUrl}`);
      }
      console.log(`==============================================\n`);
      return { success: true, messageId: info.messageId, previewUrl };
    }
  } catch (err) {
    console.warn(`Direct email dispatch failed: ${err.message}. Logging email details to console:`);
  }

  // Fallback: log to console so app never crashes
  console.log(`\n================ [CONSOLE EMAIL LOG] ================`);
  console.log(`To: ${to}`);
  console.log(`Subject: ${subject}`);
  console.log(`Content:\n${text || html}`);
  console.log(`=====================================================\n`);
  return { success: true, loggedToConsole: true };
};

const lastDevTokens = new Map();

const sendVerificationEmail = async (toEmail, toName, rawToken) => {
  lastDevTokens.set(toEmail.toLowerCase().trim(), { type: 'verify', token: rawToken, timestamp: Date.now() });
  const verifyUrl = `${config.clientUrl}/verify-email/${rawToken}`;
  const subject = 'PizzaForge - Verify Your Email Address';
  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
      <h2 style="color: #E23744; margin-top: 0;">🍕 Welcome to PizzaForge, ${toName}!</h2>
      <p style="color: #333; line-height: 1.6;">Thank you for registering. Please click the button below to verify your email address and activate your account:</p>
      <div style="text-align: center; margin: 30px 0;">
        <a href="${verifyUrl}" style="background-color: #E23744; color: white; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Verify Email</a>
      </div>
      <p style="color: #666; font-size: 14px;">Or copy and paste this link in your browser:</p>
      <p style="word-break: break-all; color: #0066cc; font-size: 13px;">${verifyUrl}</p>
      <p style="color: #888; font-size: 12px; margin-top: 30px;">This link will expire in 24 hours. If you did not create an account, you can safely ignore this email.</p>
    </div>
  `;
  return sendMail({ to: toEmail, subject, html });
};

const sendPasswordResetEmail = async (toEmail, toName, rawToken) => {
  lastDevTokens.set(toEmail.toLowerCase().trim(), { type: 'reset', token: rawToken, timestamp: Date.now() });
  const resetUrl = `${config.clientUrl}/reset-password/${rawToken}`;
  const subject = 'PizzaForge - Password Reset Request';
  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
      <h2 style="color: #E23744; margin-top: 0;">🔑 Password Reset Request</h2>
      <p style="color: #333; line-height: 1.6;">Hello ${toName || 'there'}, we received a request to reset your PizzaForge account password. Click below to set a new password:</p>
      <div style="text-align: center; margin: 30px 0;">
        <a href="${resetUrl}" style="background-color: #E23744; color: white; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Reset Password</a>
      </div>
      <p style="color: #666; font-size: 14px;">Or copy and paste this link in your browser:</p>
      <p style="word-break: break-all; color: #0066cc; font-size: 13px;">${resetUrl}</p>
      <p style="color: #888; font-size: 12px; margin-top: 30px;">This link will expire in 15 minutes. If you did not request a password reset, please ignore this email or review your account security.</p>
    </div>
  `;
  return sendMail({ to: toEmail, subject, html });
};

const sendLowStockAlertEmail = async (adminEmail, lowStockItems) => {
  const subject = '⚠️ PizzaForge Alert: Inventory Items Below Threshold';
  const rows = lowStockItems
    .map(
      (item) => `
      <tr style="border-bottom: 1px solid #eee;">
        <td style="padding: 10px; font-weight: bold;">${item.name}</td>
        <td style="padding: 10px; text-transform: capitalize;">${item.category}</td>
        <td style="padding: 10px; color: #dc2626; font-weight: bold;">${item.stock}</td>
        <td style="padding: 10px;">${item.threshold}</td>
        <td style="padding: 10px;">₹${item.price}</td>
      </tr>
    `
    )
    .join('');

  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 680px; margin: 0 auto; padding: 20px; border: 1px solid #fecaca; border-radius: 8px; background-color: #fffaf0;">
      <h2 style="color: #dc2626; margin-top: 0;">⚠️ Low Stock Inventory Alert</h2>
      <p style="color: #333;">The following <strong>${lowStockItems.length}</strong> ingredient(s) have fallen below their configured safety thresholds:</p>
      <table style="width: 100%; border-collapse: collapse; margin: 20px 0; background: #ffffff; border: 1px solid #e5e7eb;">
        <thead>
          <tr style="background-color: #f3f4f6; text-align: left;">
            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Item Name</th>
            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Category</th>
            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Current Stock</th>
            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Threshold</th>
            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Unit Price</th>
          </tr>
        </thead>
        <tbody>
          ${rows}
        </tbody>
      </table>
      <p style="color: #4b5563; font-size: 14px;">Please restock these items in the Admin Inventory Panel to avoid pizza order rejections.</p>
      <div style="margin-top: 25px; text-align: center;">
        <a href="${config.clientUrl}/admin/inventory" style="background-color: #dc2626; color: white; padding: 10px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Open Inventory Dashboard</a>
      </div>
    </div>
  `;
  return sendMail({ to: adminEmail, subject, html });
};

const getDevToken = (email) => {
  return lastDevTokens.get(email.toLowerCase().trim());
};

module.exports = {
  sendMail,
  sendVerificationEmail,
  sendPasswordResetEmail,
  sendLowStockAlertEmail,
  getDevToken
};
