import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.crypto.SealedObject;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;


public class SearchFilmHandler implements Runnable {

    private static ArrayList<SearchFilmHandler> clientList = new ArrayList<>();
    private final String clientName;
    private ObjectOutputStream objectOut = null;
    private ObjectInputStream objectIn = null;
    private Socket socket = null;
    private static final String searchURL = "https://www.imdb.com/find?q=";
    private static final String getFilmURL = "https://www.imdb.com";
    //    Object lock = new Object();
    private static int index = 0;
    private byte[] publicKey = null;
    private byte[] privateKey = null;
    private byte[] clientPublicKey = null;

    public SearchFilmHandler(Socket socket) throws IOException {
        this.socket = socket;
        objectOut = new ObjectOutputStream(socket.getOutputStream());
        objectIn = new ObjectInputStream(socket.getInputStream());
        this.clientName = "Client[" + ++index + "]";
        clientList.add(this);
    }

    public void closeConnect() {
        try {
            if (objectOut != null)
                objectOut.close();
            if (objectIn != null)
                objectIn.close();
            if (socket != null)
                socket.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public Document getDoc(String host, String searchValue) {
        try {
            return Jsoup.connect(host + searchValue)
                    .method(Connection.Method.GET)
                    .execute().parse();
        } catch (IOException e) {
            e.printStackTrace();
            closeConnect();
        }
        return null;
    }

    public void searchFilm(String searchValue) {
        Element searchElement = null;
        Element getElement;
        String link;
        Document searchDoc;
        Document getFilmDoc;
        String imdb = "None";
        String filmPoster = "None";
        String filmTrailer = "None";
        String filmGern;
        String filmDescription;
        String filmDirector;
        JSONArray reviewList = new JSONArray();
        try {
            int timeOut = 0;
            while (timeOut < 4) {
                searchDoc = getDoc(searchURL, searchValue);
                Elements checkSearch = searchDoc.body().getElementsByAttributeValue("class", "ipc-metadata-list-summary-item ipc-metadata-list-summary-item--click find-result-item find-title-result");
                if (checkSearch.size() == 0) {
                    timeOut++;
                    continue;
                }
                searchElement = checkSearch.get(0);
                break;
            }
            //
            if (searchElement != null) {
                link = searchElement.getElementsByTag("a").get(0).attr("href");
                while (true) {
                    getFilmDoc = getDoc(getFilmURL, link);
                    getElement = getFilmDoc.body().getElementsByAttributeValueContaining("class", "ipc-page-section").get(0);
                    if (getElement == null)
                        continue;
                    break;
                }
                //
                Elements checkImdb = getElement.getElementsByAttributeValue("class", "sc-bde20123-1 cMEQkK");
                if (checkImdb.size() != 0)
                    imdb = checkImdb.get(0).text();
                Elements checkPoster = getElement.getElementsByAttributeValue("class", "ipc-image");
                if (checkPoster.size() != 0)
                    filmPoster = checkPoster.get(0)
                            .attr("srcset")
                            .split(" ")[4];
                String filmTrailerLink = getElement.getElementsByAttributeValue("data-testid", "video-player-slate-overlay")
                        .attr("href");
                if (!filmTrailerLink.equals(""))
                    filmTrailer = getFilmURL + filmTrailerLink;

                Elements gernSpanTag = getElement.getElementsByAttributeValue("class", "ipc-chip-list__scroller")
                        .get(0).getElementsByTag("span");

                ArrayList<String> gernList = new ArrayList<>();
                for (Element gern : gernSpanTag) {
                    gernList.add(gern.text());
                }
                filmGern = String.join(", ", gernList);

                filmDescription = getElement.getElementsByAttributeValue("data-testid", "plot-xl").get(0).text();

                Elements directorATag = getElement.getElementsByAttributeValue("class", "ipc-inline-list ipc-inline-list--show-dividers ipc-inline-list--inline ipc-metadata-list-item__list-content baseAlt")
                        .get(0).getElementsByTag("a");

                ArrayList<String> directorList = new ArrayList<>();
                for (Element director : directorATag) {
                    directorList.add(director.text());
                }
                filmDirector = String.join(", ", directorList);
                //
                Elements reviewLinks = getElement.getElementsByAttributeValueContaining("class", "isReview");
                if (reviewLinks.size() != 0) {
                    String reviewUrl = reviewLinks.get(0).attr("href");
                    Document reviewDoc = getDoc(getFilmURL, reviewUrl);
                    Elements titles = reviewDoc.select("#main > section > div.lister > div.lister-list > div > div.review-container > div.lister-item-content > a");
                    Elements usernames = reviewDoc.select("#main > section > div.lister > div.lister-list > div > div.review-container > div.lister-item-content > div.display-name-date > span.display-name-link > a");
                    Elements contents = reviewDoc.select("#main > section > div.lister > div.lister-list > div > div.review-container > div.lister-item-content > div.content > div");

                    for (int i = 0; i < titles.size(); i++) {
                        JSONObject review = new JSONObject();
                        review.put("title", titles.get(i).text());
                        review.put("username", usernames.get(i).text());
                        review.put("content", contents.get(i).text());
                        reviewList.put(review);
                    }
                }
                //
                String filmName = searchElement.getElementsByTag("a").get(0).text();
                Elements yearUlTag = searchElement.getElementsByAttributeValue("class", "ipc-inline-list ipc-inline-list--show-dividers ipc-inline-list--no-wrap ipc-inline-list--inline ipc-metadata-list-summary-item__tl base");
                String filmYear = yearUlTag.get(0).getElementsByTag("li").get(0).text();
                String actors = "";
                Elements actorsUlTags = getElement.getElementsByAttributeValue("class", "ipc-inline-list ipc-inline-list--show-dividers ipc-inline-list--inline ipc-metadata-list-item__list-content baseAlt");
                if (actorsUlTags.size() > 2) {
                    Element actorsUlTag = actorsUlTags.get(1);
                    Elements actorsName = actorsUlTag.getElementsByTag("a");
                    String[] actorsNameArray = new String[actorsName.size()];
                    for (int i = 0; i < actorsName.size(); i++) {
                        actorsNameArray[i] = actorsName.get(i).text();
                    }
                    actors = String.join(", ", actorsNameArray);
                }
                String result = "";
                result += filmName +
                        ";;" + filmYear +
                        ";;" + actors +
                        ";;" + imdb +
                        ";;" + filmPoster +
                        ";;" + filmTrailer +
                        ";;" + filmGern +
                        ";;" + filmDescription +
                        ";;" + filmDirector +
                        ";;" + reviewList;
                String key = Cryption.RandomKey() + ";;1";
                SealedObject encryptResult = (SealedObject) Cryption.AES.EncryptionObject(result, key);
                key = Cryption.RSA.Encryption(key, clientPublicKey);
                objectOut.writeObject(encryptResult);
                objectOut.writeObject(key);
                objectOut.writeObject("done");
                objectOut.flush();
            } else {
                String result = "Không có kết quả";
                String key = Cryption.RandomKey() + ";;1";
                SealedObject encryptResult = (SealedObject) Cryption.AES.EncryptionObject(result, key);
                key = Cryption.RSA.Encryption(key, clientPublicKey);
                objectOut.writeObject(encryptResult);
                objectOut.writeObject(key);
                objectOut.writeObject("done");
                objectOut.flush();
            }

        } catch (Exception error) {
            error.printStackTrace();

        }

    }

    private HashMap<String, List> forwardImageOverNetwork(Mat img, Net dnnNet, List<String> outputLayers) {
        // --We need to prepare some data structure  in order to store the data returned by the network  (ie, after Net.forward() call))
        // So, Initialize our lists of detected bounding boxes, confidences, and  class IDs, respectively
        // This is what this method will return:
        HashMap<String, List> result = new HashMap<String, List>();
        result.put("boxes", new ArrayList<Rect2d>());
        result.put("confidences", new ArrayList<Float>());
        result.put("class_ids", new ArrayList<Integer>());

        // -- The input image to a neural network needs to be in a certain format called a blob.
        //  In this process, it scales the image pixel values to a target range of 0 to 1 using a scale factor of 1/255.
        // It also resizes the image to the given size of (416, 416) without cropping
        // Construct a blob from the input image and then perform a forward  pass of the YOLO object detector,
        // giving us our bounding boxes and  associated probabilities:

        Mat blob_from_image = Dnn.blobFromImage(img, 1 / 255.0, new Size(416, 416), // Here we supply the spatial size that the Convolutional Neural Network expects.
                new Scalar(new double[]{0.0, 0.0, 0.0}), true, false);
        dnnNet.setInput(blob_from_image);

        // -- the output from network's forward() method will contain a List of OpenCV Mat object, so lets prepare one
        List<Mat> outputs = new ArrayList<Mat>();

        // -- Finally, let pass forward throught network. The main work is done here:
        dnnNet.forward(outputs, outputLayers);

        // --Each output of the network outs (ie, each row of the Mat from 'outputs') is represented by a vector of the number
        // of classes + 5 elements.  The first 4 elements represent center_x, center_y, width and height.
        // The fifth element represents the confidence that the bounding box encloses the object.
        // The remaining elements are the confidence levels (ie object types) associated with each class.
        // The box is assigned to the category corresponding to the highest score of the box:

        for (Mat output : outputs) {
            //  loop over each of the detections. Each row is a candidate detection,
//            System.out.println("Output.rows(): " + output.rows() + ", Output.cols(): " + output.cols());
            for (int i = 0; i < output.rows(); i++) {
                Mat row = output.row(i);
                List<Float> detect = new MatOfFloat(row).toList();
                List<Float> score = detect.subList(5, output.cols());
                int class_id = argmax(score); // index maximalnog elementa liste
                float conf = score.get(class_id);
                if (conf >= 0.5) {
                    int center_x = (int) (detect.get(0) * img.cols());
                    int center_y = (int) (detect.get(1) * img.rows());
                    int width = (int) (detect.get(2) * img.cols());
                    int height = (int) (detect.get(3) * img.rows());
                    int x = (center_x - width / 2);
                    int y = (center_y - height / 2);
                    Rect2d box = new Rect2d(x, y, width, height);
                    result.get("boxes").add(box);
                    result.get("confidences").add(conf);
                    result.get("class_ids").add(class_id);
                }
            }
        }
        return result;
    }

    /**
     * Returns index of maximum element in the list
     */
    private int argmax(List<Float> array) {
        float max = array.get(0);
        int re = 0;
        for (int i = 1; i < array.size(); i++) {
            if (array.get(i) > max) {
                max = array.get(i);
                re = i;
            }
        }
        return re;
    }

    private MatOfInt getBBoxIndicesFromNonMaximumSuppression(ArrayList<Rect2d> boxes, ArrayList<Float> confidences ) {
        MatOfRect2d mOfRect = new MatOfRect2d();
        mOfRect.fromList(boxes);
        MatOfFloat mfConfs = new MatOfFloat(Converters.vector_float_to_Mat(confidences));
        MatOfInt result = new MatOfInt();
        Dnn.NMSBoxes(mOfRect, mfConfs, (float)(0.6), (float)(0.5), result);
        return result;
    }

    private Mat drawBoxesOnTheImage(Mat img, MatOfInt indices, ArrayList<Rect2d> boxes, List<String> cocoLabels, ArrayList<Integer> class_ids, ArrayList<Scalar> colors) {
        //Scalar color = new Scalar( new double[]{255, 255, 0});
        List indices_list = indices.toList();
        for (int i = 0; i < boxes.size(); i++) {
            if (indices_list.contains(i)) {
                Rect2d box = boxes.get(i);
                Point x_y = new Point(box.x, box.y);
                Point w_h = new Point(box.x + box.width, box.y + box.height);
                Point text_point = new Point(box.x, box.y - 5);
                Imgproc.rectangle(img, w_h, x_y, colors.get(class_ids.get(i)), 1);
                String label = cocoLabels.get(class_ids.get(i));
                Imgproc.putText(img, label, text_point, Imgproc.FONT_HERSHEY_SIMPLEX, 1, colors.get(class_ids.get(i)), 2);
            }
        }
        return img;
    }

    public File detectObjectOnImage(File inputImageFile) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            String path = System.getProperty("user.dir");
            //  load the COCO class labels our YOLO model was trained on
            Scanner scan = new Scanner(new FileReader(path + "/src/coco.names"));
            List<String> cocoLabels = new ArrayList<String>();
            while (scan.hasNextLine()) {
                cocoLabels.add(scan.nextLine());
            }

            //  load our YOLO object detector trained on COCO dataset
            Net dnnNet = Dnn.readNetFromDarknet(path + "/src/yolov3.cfg",
                    path + "/src/yolov3.weights");
            // YOLO on GPU:
            dnnNet.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
            dnnNet.setPreferableTarget(Dnn.DNN_TARGET_CUDA);

            // generate radnom color in order to draw bounding boxes
            Random random = new Random();
            ArrayList<Scalar> colors = new ArrayList<Scalar>();
            for (int i = 0; i < cocoLabels.size(); i++) {
//                colors.add(new Scalar(new double[]{random.nextInt(255), random.nextInt(255), random.nextInt(255)}));
                colors.add(new Scalar(0, 255, 0));
            }

            // load our input image
            Mat img = Imgcodecs.imread(inputImageFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR); // dining_table.jpg soccer.jpg baggage_claim.jpg
            //  -- determine  the output layer names that we need from YOLO
            // The forward() function in OpenCV’s Net class needs the ending layer till which it should run in the network.
            //  getUnconnectedOutLayers() vraca indexe za: yolo_82, yolo_94, yolo_106, (indexi su 82, 94 i 106) i to su poslednji layeri
            // u networku:
            List<String> layerNames = dnnNet.getLayerNames();
            List<String> outputLayers = new ArrayList<String>();
            for (
                    Integer i : dnnNet.getUnconnectedOutLayers().

                    toList()) {
                outputLayers.add(layerNames.get(i - 1));
            }

            HashMap<String, List> result = forwardImageOverNetwork(img, dnnNet, outputLayers);

            ArrayList<Rect2d> boxes = (ArrayList<Rect2d>) result.get("boxes");
            ArrayList<Float> confidences = (ArrayList<Float>) result.get("confidences");
            ArrayList<Integer> class_ids = (ArrayList<Integer>) result.get("class_ids");

            // -- Now , do so-called “non-maxima suppression”
            //Non-maximum suppression is performed on the boxes whose confidence is equal to or greater than the threshold.
            // This will reduce the number of overlapping boxes:
            MatOfInt indices = getBBoxIndicesFromNonMaximumSuppression(boxes, confidences);
            //-- Finally, go over indices in order to draw bounding boxes on the image:
            img = drawBoxesOnTheImage(img, indices, boxes, cocoLabels, class_ids, colors);
            Imgcodecs.imwrite(inputImageFile.getAbsolutePath(), img);
            return inputImageFile;
        } catch (Exception error) {
            error.printStackTrace();
        }
        return null;
    }

    public File compressImage(File inputImageFile){
        try{
            BufferedImage image = ImageIO.read(inputImageFile);

            OutputStream os = new FileOutputStream(inputImageFile);
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(getFileExtension(inputImageFile));
            ImageWriter writer = (ImageWriter) writers.next();

            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.5f);
            writer.write(null, new IIOImage(image, null, null), param);

            os.close();
            ios.close();
            writer.dispose();
            return inputImageFile;
        }
        catch(Exception error){
            error.printStackTrace();
        }
        return null;
    }

    public File greyScale(File inputImageFile) {
        if (inputImageFile != null) {
            try {
                BufferedImage imgIn = ImageIO.read(inputImageFile);
                // get image's width and height
                int width = imgIn.getWidth();
                int height = imgIn.getHeight();

                // convert to greyscale
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        // Here (x,y)denotes the coordinate of image
                        // for modifying the pixel value.
                        int p = imgIn.getRGB(x, y);
                        int a = (p >> 24) & 0xff;
                        int r = (p >> 16) & 0xff;
                        int g = (p >> 8) & 0xff;
                        int b = p & 0xff;
                        // calculate average
                        int avg = (r + g + b) / 3;
                        // replace RGB value with avg
                        p = (a << 24) | (avg << 16) | (avg << 8) | avg;
                        imgIn.setRGB(x, y, p);
                    }
                }

                // write image
//                File outputImageFile = new File(getFileName(inputImageFile) + "_greyscale." + getFileExtension(inputImageFile));
                ImageIO.write(imgIn, getFileExtension(inputImageFile), inputImageFile);
                return inputImageFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getFileName(File file) {
        return file.getName().split("\\.")[0];
    }

    public String getFileExtension(File file) {
        return file.getName().split("\\.")[1];
    }

    public void imageProcessing(File inputImageFile, String option, String fileExt) {
        try {
            File outputImageFile;
            String key;
            SealedObject result;
            switch (option) {
                case "grey-scale":
                    outputImageFile = greyScale(inputImageFile);
                    key = Cryption.RandomKey() + ";;2";
                    result = (SealedObject) Cryption.AES.EncryptionObject(outputImageFile, key);
                    key = Cryption.RSA.Encryption(key, clientPublicKey);
                    objectOut.writeObject(result);
                    objectOut.writeObject(key);
                    objectOut.writeObject("done");
                    objectOut.flush();
                    break;
                case "object-detect":
                    outputImageFile = detectObjectOnImage(inputImageFile);
                    key = Cryption.RandomKey() + ";;2";
                    result = (SealedObject) Cryption.AES.EncryptionObject(outputImageFile, key);
                    key = Cryption.RSA.Encryption(key, clientPublicKey);
                    objectOut.writeObject(result);
                    objectOut.writeObject(key);
                    objectOut.writeObject("done");
                    objectOut.flush();
                    break;
                case "compress":
                    outputImageFile = compressImage(inputImageFile);
                    key = Cryption.RandomKey() + ";;2";
                    result = (SealedObject) Cryption.AES.EncryptionObject(outputImageFile, key);
                    key = Cryption.RSA.Encryption(key, clientPublicKey);
                    objectOut.writeObject(result);
                    objectOut.writeObject(key);
                    objectOut.writeObject("done");
                    objectOut.flush();
                    break;
                case "change-extension":
                    outputImageFile = changeExtension(inputImageFile, fileExt);
                    key = Cryption.RandomKey() + ";;2";
                    result = (SealedObject) Cryption.AES.EncryptionObject(outputImageFile, key);
                    key = Cryption.RSA.Encryption(key, clientPublicKey);
                    objectOut.writeObject(result);
                    objectOut.writeObject(key);
                    objectOut.writeObject("done");
                    objectOut.flush();
                    break;
                default:
            }

        } catch (Exception error) {
            error.printStackTrace();
        }

    }

    public File changeExtension(File inputImageFile, String fileExt){
        try{
            BufferedImage imgIn = ImageIO.read(inputImageFile);
            inputImageFile = new File(inputImageFile.getParentFile() + "/" + getFileName(inputImageFile) + "." + fileExt);
            ImageIO.write(imgIn, fileExt, inputImageFile);
            return inputImageFile;
        }
        catch(Exception error){
            error.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        ArrayList<byte[]> keyList = null;
        if (socket.isConnected()) {
            try {
                keyList = Cryption.RSA.KeyPairGenerate();
                publicKey = keyList.get(0);
                privateKey = keyList.get(1);
                objectOut.writeObject(publicKey);
                objectOut.flush();
                clientPublicKey = (byte[]) objectIn.readObject();
            } catch (Exception error) {
                error.printStackTrace();
            }
        }
        while (socket.isConnected()) {
            try {
                Object requestFromClient;
                SealedObject message = null;
                while ((requestFromClient = (Object) objectIn.readObject()) != null) {

                    String content;
                    String key = null;
                    if (requestFromClient.getClass().getName().equalsIgnoreCase("javax.crypto.SealedObject")) {
                        message = (SealedObject) requestFromClient;
                    } else {
                        key = (String) requestFromClient;
                    }
                    if (message != null && key != null) {
                        key = Cryption.RSA.Decryption(key, privateKey);
                        String[] keyPart = key.split(";;");
                        if (keyPart[1].equalsIgnoreCase("1")) {
                            content = (String) Cryption.AES.DecryptionObject(message, key);
                            String searchValue = content;
                            String[] tmp = searchValue.split(" ");
                            searchValue = "";
                            searchValue = String.join("+", tmp);
                            searchValue += "&ref_=nv_sr_sm";
                            System.out.println(clientName + ": " + searchValue);
                            searchFilm(searchValue);
                        } else {
                            System.out.println("Xử lí ảnh...");
                            String option = keyPart[2];
                            String fileExt = keyPart[3];
                            File inputImageFile = (File) Cryption.AES.DecryptionObject(message, key);
                            imageProcessing(inputImageFile, option, fileExt);
                            System.out.println("Hoàn thành!");
                        }
                    }

                }
            } catch (Exception error) {
//                error.printStackTrace();
                closeConnect();
                break;
            }
        }
    }
}
