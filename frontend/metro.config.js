const { getDefaultConfig } = require('expo/metro-config');

const config = getDefaultConfig(__dirname);

// Enable environment variables from .env file
config.resolver.enablePlatformPackages = true;

// Make sure .env variables are available at runtime
config.resolver.extraNodeModules = {
  ...config.resolver.extraNodeModules,
};

module.exports = config;
