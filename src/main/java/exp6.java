import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class exp6 {
    /**
     * @param args
     */
    private static int threadCount = 3;
    private static int blockSize;
    private static String path = "http://ali-cdn.kwai.net/upic/2018/08/19/20/BMjAxODA4MTkyMDAzNDFfMTA2NDUzNDM1MF83NjczNjAzMjgyXzBfMw==_b_Bfcfef700680297b4c5bd444cc54caeef.mp4";
    private static int currentRunThreadCount = 0;

    public static void main1(String[] args) {
        try {
            System.setProperty("http.proxyHost", "127.0.0.1");
            System.setProperty("http.proxyPort", "1080");

            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10 * 1000);
            int code = connection.getResponseCode();
            if (code == 200) {
                int fileLength = connection.getContentLength();
                RandomAccessFile randomAccessFile = new RandomAccessFile(new File(getFileName(path)), "rw");
                randomAccessFile.setLength(fileLength);
                blockSize = fileLength / threadCount;
                for (int i = 0; i < threadCount; i++) {
                    int startThread = i * blockSize;
                    int endThread = (i + 1) * blockSize - 1;
                    if (i == blockSize - 1) endThread = fileLength - 1;
                    new DownloadThread(i, startThread, endThread).start();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class DownloadThread extends Thread {
        private int threadId;
        private int endThread;
        private int startThred;

        public DownloadThread(int threadId, int startThred, int endThread) {
            this.threadId = threadId;
            this.startThred = startThred;
            this.endThread = endThread;
        }

        public void run() {
            synchronized (DownloadThread.class) {
                currentRunThreadCount += 1;
            }
            //分段请求网络连接，分段保存在本地
            try {
                System.err.println("理论线程:" + threadId + ",开始位置:" + startThred + ",结束位置:" + endThread);
                URL url = new URL(path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10 * 1000);
//                String partFileName = getFileName(path) + threadId;
                File file = new File("F://kwaiMvDownload//" + threadId + ".mp4");
                if (file.exists()) {    //是否断点
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    String lastPostion_str = bufferedReader.readLine();
                    startThred = Integer.parseInt(lastPostion_str);
                    bufferedReader.close();
                }
                //设置分段下载的头信息  Range:做分段
                connection.setRequestProperty("Range", "bytes:" + startThred + "-" + endThread);
                int code = connection.getResponseCode();
                System.out.println(code);
                if (code == 200) {    //200:请求全部资源成功  206:代表部分资源请求成功
                    InputStream inputStream = connection.getInputStream();
                    System.out.println(getFileName(path));
                    RandomAccessFile randomAccessFile = new RandomAccessFile(new File("F://kwaiMvDownload//" + getFileName(path)), "rw");
                    randomAccessFile.seek(startThred);
                    byte[] buffer = new byte[1024 * 10];
                    int length = -1;
                    int total = 0;//记录下载的总量
                    System.err.println("实际线程:" + threadId + ",开始位置:" + startThred + ",结束位置:" + endThread);
                    while ((length = inputStream.read(buffer)) != -1) {
                        randomAccessFile.write(buffer, 0, length);
                        total += length;
                        int currentThreadPostion = startThred + total;
                        RandomAccessFile randomAccessFile2 = new RandomAccessFile(file, "rwd");
                        randomAccessFile2.write(String.valueOf(currentThreadPostion).getBytes());
                        randomAccessFile2.close();
                    }
                    randomAccessFile.close();
                    inputStream.close();
                    System.err.println("线程:" + threadId + "下载完毕");
                    synchronized (DownloadThread.class) {
                        currentRunThreadCount -= 1;
                        if (currentRunThreadCount == 0) {
                            for (int i = 0; i < threadCount; i++) {
//                                String partFileName1 = getFileName(path) + i ;
//                                File file2 = new File("F://kwaiMvDownload//" + partFileName1 + ".mp4");
                                File file2 = new File("F://kwaiMvDownload//" + i + ".mp4");
                                file2.delete();
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            super.run();
        }
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }



//    public static class DownloadThread extends Thread {
//        private int threadId;
//        private int endThread;
//        private int startThread;
//        private String downloadPath;
//        private CurrentMvDownLoadState mDone;
//
//        public interface CurrentMvDownLoadState {
//            void onMvDownloadDone(boolean isDown);
//        }
//
//        public DownloadThread(int threadId, int startThread, int endThread, String downloadPath, CurrentMvDownLoadState done) {
//            this.threadId = threadId;
//            this.startThread = startThread;
//            this.endThread = endThread;
//            this.downloadPath = downloadPath;
//            this.mDone = done;
//        }
//
//        public void run() {
//            synchronized (DownloadThread.class) {
//                currentRunThreadCount += 1;
//            }
//            //分段请求网络连接，分段保存在本地
//            try {
//                System.err.println("理论线程:" + threadId + ",开始位置:" + startThread + ",结束位置:" + endThread);
//                URL url = new URL(downloadPath);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setConnectTimeout(10 * 1000);
////                String partFileName = getFileName(downloadPath) + threadId;
//                File file = new File(ROOT_PATH + threadId + ".mp4");
//                if (file.exists()) {    //是否断点
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//                    String lastPostion_str = bufferedReader.readLine();
//                    startThread = Integer.parseInt(lastPostion_str);
//                    bufferedReader.close();
//                }
//                //设置分段下载的头信息  Range:做分段
//                connection.setRequestProperty("Range", "bytes:" + startThread + "-" + endThread);
//                int code = connection.getResponseCode();
//                System.out.println(code);
//                if (code == 200) {    //200:请求全部资源成功  206:代表部分资源请求成功
//                    InputStream inputStream = connection.getInputStream();
//                    System.out.println(getFileName(downloadPath));
//                    RandomAccessFile randomAccessFile = new RandomAccessFile(new File(ROOT_PATH + getFileName(downloadPath)), "rw");
//                    randomAccessFile.seek(startThread);
//                    byte[] buffer = new byte[1024 * 10];
//                    int length = -1;
//                    int total = 0;//记录下载的总量
//                    System.err.println("实际线程:" + threadId + ",开始位置:" + startThread + ",结束位置:" + endThread);
//                    while ((length = inputStream.read(buffer)) != -1) {
//                        randomAccessFile.write(buffer, 0, length);
//                        total += length;
//                        int currentThreadPostion = startThread + total;
//                        RandomAccessFile randomAccessFile2 = new RandomAccessFile(file, "rwd");
//                        randomAccessFile2.write(String.valueOf(currentThreadPostion).getBytes());
//                        randomAccessFile2.close();
//                    }
//                    randomAccessFile.close();
//                    inputStream.close();
//                    System.err.println("线程:" + threadId + "下载完毕");
//                    synchronized (DownloadThread.class) {
//                        currentRunThreadCount -= 1;
//                        if (currentRunThreadCount == 0) {
//                            mDone.onMvDownloadDone(true);
//                            for (int i = 0; i < threadCount; i++) {
////                                String partFileName1 = getFileName(downloadPath) + i;
//                                File file2 = new File(ROOT_PATH + i + ".mp4");
//                                file2.delete();
//                            }
//                        }
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            super.run();
//        }
//
//        public synchronized static void downloadMV(String downLoadPath) {
//            try {
//                System.setProperty("http.proxyHost", "127.0.0.1");
//                System.setProperty("http.proxyPort", "1080");
//
//                URL url = new URL(downLoadPath);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setConnectTimeout(10 * 1000);
//                int code = connection.getResponseCode();
//                if (code == 200) {
//                    int fileLength = connection.getContentLength();
//                    RandomAccessFile randomAccessFile = new RandomAccessFile(ROOT_PATH + new File(getFileName(downLoadPath)), "rw");
//                    randomAccessFile.setLength(fileLength);
//                    blockSize = fileLength / threadCount;
//                    for (int i = 0; i < threadCount; i++) {
//                        final boolean[] isDownLoadDown = {false};
//                        int startThread = i * blockSize;
//                        int endThread = (i + 1) * blockSize - 1;
//                        if (i == blockSize - 1) endThread = fileLength - 1;
//                        DownloadThread thread = new DownloadThread(i, startThread, endThread, downLoadPath, new DownloadThread.CurrentMvDownLoadState() {
//                            public void onMvDownloadDone(boolean isDown) {
//                                isDownLoadDown[0] = isDown;
//                            }
//                        });
//                        thread.start();
//
//                        System.out.println("*******************   " + isDownLoadDown[0] + "     " + i);
//                        while (!isDownLoadDown[0]) {
//                            System.out.println("等待");
//                            Thread.sleep(1000);
//                        }
//                    }
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static String getFileName(String path) {
//        String subPath = path.substring(path.lastIndexOf("/") + 1);
//        System.out.println(subPath);
//        return subPath;
//    }

}