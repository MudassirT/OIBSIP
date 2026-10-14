const cron = require('node-cron');
const config = require('../config/env');
const { checkAndNotifyLowStock } = require('./inventoryService');

let cronJob = null;

const initCronJobs = () => {
  const schedule = config.cronSchedule;

  if (!cron.validate(schedule)) {
    console.warn(`Invalid CRON_SCHEDULE: "${schedule}". Defaulting to hourly "0 * * * *"`);
  }

  const validSchedule = cron.validate(schedule) ? schedule : '0 * * * *';

  cronJob = cron.schedule(validSchedule, async () => {
    console.log(`[CRON] Executing scheduled low-stock inventory check (${validSchedule})...`);
    try {
      const result = await checkAndNotifyLowStock(false);
      console.log(`[CRON] Completed low-stock check. Items alerted: ${result.count}`);
    } catch (err) {
      console.error('[CRON] Error during scheduled inventory check:', err.message);
    }
  });

  console.log(`Low-stock cron job scheduled with pattern: "${validSchedule}"`);
};

module.exports = { initCronJobs };
