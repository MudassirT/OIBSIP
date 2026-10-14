const mongoose = require('mongoose');
const config = require('./env');

let memServerInstance = null;

const connectDB = async () => {
  try {
    let uri = config.mongoUri;

    if (uri) {
      console.log(`Connecting to MongoDB at: ${uri.replace(/\/\/[^@]*@/, '//***@')}`);
      try {
        await mongoose.connect(uri, {
          serverSelectionTimeoutMS: 4000
        });
        console.log('MongoDB successfully connected.');
        return;
      } catch (err) {
        console.warn(`Could not connect to specified MONGO_URI (${err.message}).`);
        if (config.nodeEnv === 'production') {
          throw err;
        }
      }
    }

    // Development fallback using mongodb-memory-server
    console.log('Starting in-memory MongoDB instance for development & testing...');
    const { MongoMemoryServer } = require('mongodb-memory-server');
    memServerInstance = await MongoMemoryServer.create();
    const memUri = memServerInstance.getUri();
    console.log(`Connected to In-Memory MongoDB at: ${memUri}`);

    await mongoose.connect(memUri);
    console.log('In-Memory MongoDB successfully connected.');
  } catch (error) {
    console.error('Fatal: MongoDB connection failure:', error);
    process.exit(1);
  }
};

const disconnectDB = async () => {
  await mongoose.disconnect();
  if (memServerInstance) {
    await memServerInstance.stop();
  }
};

module.exports = { connectDB, disconnectDB };
