const childProcess = require('child_process');
const fileSystem = require('fs');
const http = require('http');
const net = require('net');
const path = require('path');

const exampleRoot = path.resolve(__dirname, '..');
const reactNativeCliPath = path.join(
  exampleRoot,
  'node_modules',
  'react-native',
  'cli.js'
);

const androidSdkPath =
  process.env.ANDROID_HOME ||
  process.env.ANDROID_SDK_ROOT ||
  (process.platform === 'win32' && process.env.LOCALAPPDATA
    ? path.join(process.env.LOCALAPPDATA, 'Android', 'Sdk')
    : undefined);

if (androidSdkPath) {
  const platformToolsPath = path.join(androidSdkPath, 'platform-tools');

  if (fileSystem.existsSync(platformToolsPath)) {
    process.env.PATH = [platformToolsPath, process.env.PATH]
      .filter(Boolean)
      .join(path.delimiter);
  }

  process.env.ANDROID_HOME = process.env.ANDROID_HOME || androidSdkPath;
  process.env.ANDROID_SDK_ROOT = process.env.ANDROID_SDK_ROOT || androidSdkPath;
}

const runAndroidArguments = ['run-android', ...process.argv.slice(2)];

function hasArgument(argumentName) {
  return runAndroidArguments.some(
    (argument) =>
      argument === argumentName || argument.startsWith(`${argumentName}=`)
  );
}

function isPortAcceptingConnections(port) {
  return new Promise((resolve) => {
    const socket = net.createConnection({
      host: '127.0.0.1',
      port,
    });

    socket.once('connect', () => {
      socket.destroy();
      resolve(true);
    });

    socket.once('error', () => {
      resolve(false);
    });

    socket.setTimeout(500, () => {
      socket.destroy();
      resolve(false);
    });
  });
}

function isMetroRunning(port) {
  return new Promise((resolve) => {
    const request = http.get(
      {
        host: '127.0.0.1',
        port,
        path: '/status',
        timeout: 500,
      },
      (response) => {
        let responseBody = '';

        response.on('data', (chunk) => {
          responseBody += chunk;
        });

        response.on('end', () => {
          resolve(responseBody.includes('packager-status:running'));
        });
      }
    );

    request.on('error', () => {
      resolve(false);
    });

    request.on('timeout', () => {
      request.destroy();
      resolve(false);
    });
  });
}

async function runAndroid() {
  if (!hasArgument('--no-packager') && !hasArgument('--port')) {
    const isDefaultMetroPortInUse = await isPortAcceptingConnections(8081);
    const isDefaultMetroRunning =
      isDefaultMetroPortInUse && (await isMetroRunning(8081));

    if (isDefaultMetroRunning) {
      runAndroidArguments.push('--no-packager');
    }
  }

  const result = childProcess.spawnSync(
    process.execPath,
    [reactNativeCliPath, ...runAndroidArguments],
    {
      cwd: exampleRoot,
      env: process.env,
      stdio: 'inherit',
    }
  );

  process.exit(result.status ?? 1);
}

runAndroid();
