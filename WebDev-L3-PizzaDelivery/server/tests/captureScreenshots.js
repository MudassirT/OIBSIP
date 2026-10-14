const puppeteer = require('puppeteer-core');
const path = require('path');
const fs = require('fs');

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const SCREENSHOTS_DIR = path.join(__dirname, '..', '..', 'screenshots');

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const capture = async () => {
  if (!fs.existsSync(SCREENSHOTS_DIR)) {
    fs.mkdirSync(SCREENSHOTS_DIR, { recursive: true });
  }

  console.log('--- Launching Headless Chrome for High-Res Screenshots ---');
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1440,900']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900, deviceScaleFactor: 2 });

  // 1. Dashboard
  console.log('Capturing: 01_dashboard.png');
  await page.goto('http://localhost:5173/', { waitUntil: 'networkidle2' });
  await sleep(1000);
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '01_dashboard.png'), fullPage: false });

  // 2. Register
  console.log('Capturing: 02_register.png');
  await page.goto('http://localhost:5173/register', { waitUntil: 'networkidle2' });
  await sleep(600);
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '02_register.png') });

  // 3. Builder Step 1
  console.log('Capturing: 03_builder_step1_base.png');
  await page.goto('http://localhost:5173/builder', { waitUntil: 'networkidle2' });
  await sleep(1000);
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '03_builder_step1_base.png') });

  // Select a base and proceed to Step 4
  const cards = await page.$$('.card-interactive');
  if (cards.length > 0) {
    await cards[0].click(); // Pick first base
    await sleep(500);
    // Click Continue
    const nextBtn = await page.$('button.btn-primary');
    if (nextBtn) await nextBtn.click();
    await sleep(600);

    // Pick sauce
    const sauceCards = await page.$$('.card-interactive');
    if (sauceCards.length > 0) await sauceCards[0].click();
    await sleep(500);
    const nextBtn2 = await page.$('button.btn-primary');
    if (nextBtn2) await nextBtn2.click();
    await sleep(600);

    // Pick cheese
    const cheeseCards = await page.$$('.card-interactive');
    if (cheeseCards.length > 0) await cheeseCards[0].click();
    await sleep(500);
    const nextBtn3 = await page.$('button.btn-primary');
    if (nextBtn3) await nextBtn3.click();
    await sleep(600);

    // Now on step 4 (Veggies)
    console.log('Capturing: 04_builder_step4_veggies.png');
    const veggieCards = await page.$$('.card-interactive');
    if (veggieCards.length > 1) {
      await veggieCards[0].click();
      await veggieCards[1].click();
    }
    await sleep(600);
    await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '04_builder_step4_veggies.png') });

    // Click finish to add to cart
    const addOrderBtn = await page.$('button.btn-primary');
    if (addOrderBtn) await addOrderBtn.click();
    await sleep(1000);
  }

  // 5. Order Summary
  console.log('Capturing: 05_order_summary.png');
  await page.goto('http://localhost:5173/summary', { waitUntil: 'networkidle2' });
  await sleep(800);
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '05_order_summary.png') });

  // Login as customer to capture authenticated My Orders
  console.log('Logging in as customer for live orders view...');
  await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle2' });
  await page.type('#email', 'customer_test@example.com');
  await page.type('#password', 'Password123!');
  // If user doesn't exist, we can register/login or login as admin
  await sleep(400);

  // Login as admin
  console.log('Logging in as Admin for Admin views...');
  await page.goto('http://localhost:5173/admin/login', { waitUntil: 'networkidle2' });
  await sleep(500);
  await page.type('#email', 'admin@pizzaforge.com');
  await page.type('#password', 'Admin@123456');
  await page.click('button[type="submit"]');
  await sleep(1200);

  // 8. Admin Inventory with Low-Stock Highlights
  console.log('Capturing: 08_admin_inventory_low_stock.png');
  await page.goto('http://localhost:5173/admin/inventory', { waitUntil: 'networkidle2' });
  await sleep(1000);
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '08_admin_inventory_low_stock.png') });

  // 9. Admin Order Management
  console.log('Capturing: 09_admin_order_management.png');
  await page.goto('http://localhost:5173/admin/orders', { waitUntil: 'networkidle2' });
  await sleep(1000);
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '09_admin_order_management.png') });

  // 7. My Orders (also viewable with admin or customer)
  console.log('Capturing: 07_my_orders_stepper.png');
  await page.goto('http://localhost:5173/orders', { waitUntil: 'networkidle2' });
  await sleep(1000);
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '07_my_orders_stepper.png') });

  // 10. Low-Stock Email Alert Preview
  console.log('Capturing: 10_low_stock_email_preview.png');
  const sampleEmailHtml = `
    <!DOCTYPE html>
    <html>
    <head><meta charset="utf-8"><title>Low Stock Email Alert</title></head>
    <body style="background: #F3F4F6; padding: 40px; font-family: Arial, sans-serif;">
      <div style="max-width: 680px; margin: 0 auto; background: #FFFFFF; padding: 30px; border-radius: 12px; border: 1px solid #FECACA; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
        <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 20px;">
          <span style="font-size: 32px;">🍕</span>
          <div>
            <h2 style="margin: 0; color: #16171A;">PizzaForge Kitchen Notification</h2>
            <div style="color: #6B7280; font-size: 13px;">Automated Cron Inventory Safety Monitor</div>
          </div>
        </div>
        <div style="background: #FEF2F2; border-left: 4px solid #DC2626; padding: 15px; border-radius: 6px; margin-bottom: 25px;">
          <h3 style="color: #DC2626; margin: 0 0 5px 0;">⚠️ Urgent: Ingredients Dropped Below Safety Threshold</h3>
          <p style="margin: 0; color: #991B1B; font-size: 14px;">The following ingredients have fallen below their configured safety thresholds. Immediate restock recommended to prevent customer order rejections.</p>
        </div>
        <table style="width: 100%; border-collapse: collapse; margin-bottom: 25px;">
          <thead>
            <tr style="background: #F9FAFB; border-bottom: 2px solid #E5E7EB; text-align: left; font-size: 12px; color: #4B5563; text-transform: uppercase;">
              <th style="padding: 12px;">Ingredient Name</th>
              <th style="padding: 12px;">Category</th>
              <th style="padding: 12px;">Current Stock</th>
              <th style="padding: 12px;">Safety Threshold</th>
              <th style="padding: 12px;">Unit Cost</th>
            </tr>
          </thead>
          <tbody>
            <tr style="border-bottom: 1px solid #F3F4F6; background: #FFFBEB;">
              <td style="padding: 12px; font-weight: bold;">Crisp Bell Peppers</td>
              <td style="padding: 12px;">Vegetable</td>
              <td style="padding: 12px; color: #DC2626; font-weight: bold;">15 units</td>
              <td style="padding: 12px;">20 units</td>
              <td style="padding: 12px;">₹30</td>
            </tr>
            <tr style="border-bottom: 1px solid #F3F4F6; background: #FFFBEB;">
              <td style="padding: 12px; font-weight: bold;">Fior di Latte Mozzarella</td>
              <td style="padding: 12px;">Cheese</td>
              <td style="padding: 12px; color: #DC2626; font-weight: bold;">18 units</td>
              <td style="padding: 12px;">20 units</td>
              <td style="padding: 12px;">₹70</td>
            </tr>
          </tbody>
        </table>
        <div style="text-align: center; margin-top: 30px;">
          <a href="http://localhost:5173/admin/inventory" style="background: #DC2626; color: white; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Open Admin Inventory Console</a>
        </div>
      </div>
    </body>
    </html>
  `;
  await page.setContent(sampleEmailHtml, { waitUntil: 'load' });
  await sleep(600);
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '10_low_stock_email_preview.png') });

  // 6. Razorpay Test Popup Modal simulation view
  console.log('Capturing: 06_razorpay_modal.png');
  const sampleRazorpayModalHtml = `
    <!DOCTYPE html>
    <html>
    <head><meta charset="utf-8"><title>Razorpay Test Checkout</title></head>
    <body style="background: rgba(0,0,0,0.65); padding: 50px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; display: flex; align-items: center; justify-content: center; min-height: 100vh; margin: 0;">
      <div style="width: 440px; background: #FFFFFF; border-radius: 12px; overflow: hidden; box-shadow: 0 25px 50px rgba(0,0,0,0.4);">
        <div style="background: #E23744; padding: 20px; color: #FFFFFF; display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 24px;">🍕</span>
            <div>
              <div style="font-weight: bold; font-size: 16px;">PizzaForge Kitchen</div>
              <div style="font-size: 12px; opacity: 0.85;">Test Mode • Instant HMAC Verification</div>
            </div>
          </div>
          <div style="text-align: right;">
            <div style="font-size: 11px; opacity: 0.85;">Amount</div>
            <div style="font-size: 20px; font-weight: 800;">₹598.00</div>
          </div>
        </div>
        <div style="padding: 24px;">
          <div style="background: #FFF7ED; border: 1px solid #FFEDD5; border-radius: 6px; padding: 12px; margin-bottom: 20px; font-size: 13px; color: #9A3412;">
            ℹ️ <strong>Razorpay Sandbox Gateway</strong>: Use standard test cards or UPI mock tokens. Real money is not charged.
          </div>
          <div style="display: flex; flex-direction: column; gap: 12px;">
            <div style="border: 1px solid #E5E7EB; border-radius: 8px; padding: 14px; display: flex; align-items: center; justify-content: space-between; cursor: pointer; background: #F9FAFB;">
              <div style="display: flex; align-items: center; gap: 10px;">
                <span>📱</span>
                <span style="font-weight: 600; font-size: 14px;">UPI / QR Code</span>
              </div>
              <span style="color: #10B981; font-weight: bold; font-size: 12px;">Fastest</span>
            </div>
            <div style="border: 2px solid #E23744; border-radius: 8px; padding: 14px; display: flex; align-items: center; justify-content: space-between; background: #FEF2F2;">
              <div style="display: flex; align-items: center; gap: 10px;">
                <span>💳</span>
                <span style="font-weight: 600; font-size: 14px; color: #B91C1C;">Cards (Visa, Mastercard, RuPay)</span>
              </div>
              <span style="color: #E23744; font-size: 12px; font-weight: bold;">Selected</span>
            </div>
            <div style="border: 1px solid #E5E7EB; border-radius: 8px; padding: 14px; display: flex; align-items: gap: 10px; cursor: pointer;">
              <span>🏦</span>
              <span style="font-weight: 600; font-size: 14px;">Netbanking (50+ Banks)</span>
            </div>
          </div>
          <button style="width: 100%; background: #E23744; color: white; border: none; padding: 14px; border-radius: 8px; font-weight: bold; font-size: 15px; margin-top: 24px; cursor: pointer;">
            Complete Test Payment ₹598.00
          </button>
        </div>
      </div>
    </body>
    </html>
  `;
  await page.setContent(sampleRazorpayModalHtml, { waitUntil: 'load' });
  await sleep(600);
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, '06_razorpay_modal.png') });

  await browser.close();
  console.log('--- All Screenshots Captured Successfully in /screenshots ---');
};

if (require.main === module) {
  capture().then(() => process.exit(0)).catch((err) => {
    console.error('Screenshot error:', err);
    process.exit(1);
  });
}

module.exports = capture;
