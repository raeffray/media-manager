const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const fs = require('fs');
const ProgressBar = require('cli-progress');
const path = require('path');
const hasha = require('hasha');
const Buffer = require('buffer').Buffer;

const check = async (filePath) => {
    const hash = await hasha.fromFile(filePath, { algorithm: 'sha256' });
    return hash + ' sha256';
};


const extractError = (err) => err.message ? err.message : err;

class MediaService {
    constructor() {
        this.chunkSize = 256000;

        const SERVICE_DEFINITION = path.join(
            __dirname,
            'ext/proto/media-services.proto'
        );
        
        const RPC_SERVER =
            process.env.MEDIA_SERVICE_ENDPOINT || 'localhost:50060';

        const packageDefinition = protoLoader.loadSync(SERVICE_DEFINITION);
        const protoDescriptor = grpc.loadPackageDefinition(packageDefinition);

        this.mediaServiceClient = new protoDescriptor.MediaService(
            RPC_SERVER,
            grpc.credentials.createInsecure()
        );
    }

    listMedias = () => {
        return new Promise((resolve, reject) => {
            this.mediaServiceClient.listMedias(
                {
                    originalName: 'fileName',
                },
                (err, data) => {
                    if (err !== null) {
                        reject(extractError(err));
                    } else {
                        resolve(JSON.stringify(data, null, 2));
                    }
                }
            );
        });
    };

    deleteMedia = (fileName) => {
        return new Promise((resolve, reject) => {
            this.mediaServiceClient.deleteMedia(
                {
                    originalName: fileName.split('/').pop(),
                },
                (err, data) => {
                    if (err !== null) {
                        if (err.code && err.code === 5) {
                            reject(`Media [${fileName}] not found to be deleted`);
                        } else (
                            reject(extractError(err))
                        )

                    } else {
                        resolve(data);
                    }
                }
            );
        });
    };

    getMedia = async (fileNamePristine) => {
        return new Promise((resolve, reject) => {
            let bar = new ProgressBar.SingleBar(
                ProgressBar.Presets.shades_classic
            );
            try {
                const fileName = fileNamePristine.split('/').pop();

                let barStarted = false;

                const currentDir = process.cwd();

                const streamMedia = this.mediaServiceClient.getStreamMedia({
                    originalName: fileName,
                });

                const tempFileName = `${currentDir}/${fileName}.dowloading`;
                const writeStream = fs.createWriteStream(tempFileName);

                streamMedia.on('data', async (response) => {
                    if (!barStarted) {
                        bar.start(response.totalSize, 0);
                    }
                    barStarted = true;
                    bar.increment(response.content.length);

                    streamMedia.pause();
                    await new Promise((resolve, reject) => {
                        writeStream.write(
                            Buffer.from(response.content, 'base64'),
                            (err) => {
                                if (err) {
                                    reject(extractError(err))
                                } else {
                                    resolve();
                                }
                            }
                        );
                    });
                    streamMedia.resume();
                });
                
                streamMedia.on('error', err => {
                    reject(extractError(err))
                })

                streamMedia.on('end', () => {
                    writeStream.end();
                    bar.stop();
                    fs.rename(
                        tempFileName,
                        `${currentDir}/${fileName}`,
                        (err) => {
                            if (err) {
                                reject(extractError(err))
                            } else {
                                resolve('Dowload finished !');
                            }
                        }
                    );
                });

                writeStream.on('error', (err) => {
                    bar.stop();
                    reject(extractError(err))
                });
            } catch (error) {
                reject(extractError(err))
            }
        });
    };

    createMedia = async (filePath) => {
        return new Promise(async (resolve, reject) => {
            let error = false;

            let bar = new ProgressBar.SingleBar(
                ProgressBar.Presets.shades_classic
            );
            try {
                const hash = await check(filePath);

                const createStreamMedia =
                    this.mediaServiceClient.createStreamMedia(
                        (err, response) => {
                            if (err) {
                                bar.stop();
                                error = true;
                                reject(extractError(err))
                            } else {
                                bar.stop();
                                resolve(
                                    `Upload finished ! Server response: ${JSON.stringify(
                                        response
                                    )}`
                                );
                            }
                        }
                    );

                const file = fs.createReadStream(filePath, {
                    highWaterMark: this.chunkSize,
                });

                bar.start(fs.statSync(filePath).size, 0);

                file.on('data', async (chunk) => {
                    if (error) {
                        file.close();
                    }
                    file.pause();
                    await new Promise((resolve, reject) => {
                        createStreamMedia.write(
                            {
                                originalName: filePath.split('/').pop(),
                                hash: hash,
                                content: chunk,
                            },
                            (err) => {
                                if (err) {
                                    reject(extractError(err))
                                } else {
                                    resolve();
                                }
                            }
                        );
                    });
                    bar.increment(chunk.length);
                    file.resume();
                });

                file.on('end', () => {
                    createStreamMedia.end();
                });
            } catch (error) {
                bar.stop();
                reject(extractError(error))
            }
        });
    };
}

module.exports = MediaService;
