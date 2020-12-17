package image;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

import java.io.File;
import java.io.FileOutputStream;

public class Img2PdfUtil{
    public static void createPDF(String outPdfFilepath, String inputFile) throws Exception{
        File file = new File(outPdfFilepath);
        // 第一步：创建一个document对象。
        Document document = new Document();
//        Document document = new Document(new RectangleReadOnly(842F, 595F));
        document.setMargins(0, 0, 0, 0);
        String path = "C:/WINDOWS/Fonts/simhei.ttf";//windows里的字体资源路径
        BaseFont bf = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        Font font = new Font(bf, 80f, Font.NORMAL, BaseColor.BLACK);
        // 创建一个PdfWriter实例，
        PdfWriter.getInstance(document, new FileOutputStream(file));
        // 第三步：打开文档。
        document.open();

        File input = new File(inputFile);
        File[] fs = input.listFiles();
        for(File xms : fs){
            if (xms.isDirectory()) {
                String baseXM = xms.getName();
                File[] fs2 = xms.listFiles();
                for(File yf : fs2){
                    if(yf.isDirectory()){
//                        Chunk glue = new Chunk(new VerticalPositionMark());
                        Paragraph paragraph = new Paragraph("\n" + "    " + xms.getName() + "\n" + "    " + yf.getName(), font);
//                        paragraph.add(glue);
//                        paragraph.setSpacingAfter(10);//距离后面行距
                        document.setPageSize(new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth()));
                        document.newPage();
                        document.add(paragraph);
                        document.newPage();


                        File[] imageFiles = yf.listFiles();
                        int len = imageFiles.length;
                        for (int i = 0; i < len; i++) {
                            if (imageFiles[i].getName().toLowerCase().endsWith(".bmp")
                                    || imageFiles[i].getName().toLowerCase().endsWith(".jpg")
                                    || imageFiles[i].getName().toLowerCase().endsWith(".jpeg")
                                    || imageFiles[i].getName().toLowerCase().endsWith(".gif")
                                    || imageFiles[i].getName().toLowerCase().endsWith(".png")) {
                                System.out.println(imageFiles[i]);
                                String temp = imageFiles[i].getAbsolutePath();
                                Image img = Image.getInstance(temp);
                                float width = img.getWidth();
                                float height = img.getHeight();
                                img.setAlignment(Image.ALIGN_CENTER);
                                img.setAbsolutePosition(0, 0);

                                if (width > height){
                                    img.setRotationDegrees(270);
                                    img.scaleAbsolute(PageSize.A4.getHeight(), PageSize.A4.getWidth());
                                }else {
                                    img.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());// 直接设定显示尺寸
                                }
                                // 根据图片大小设置页面，一定要先设置页面，再newPage（），否则无效
//                                document.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
//                                document.setPageSize(new Rectangle(597, 844));
                                document.setPageSize(new Rectangle(PageSize.A4.getWidth(), PageSize.A4.getHeight()));
                                document.newPage();
                                document.add(img);
                            }
                        }
                    }
                }
            }
        }
        document.close();

    }

    public static void main(String[] args) throws Exception {
        String outPdfPath = "C:\\Users\\duiya\\Desktop\\TTT\\Img2pdf.pdf";
        String inputFile = "C:\\Users\\duiya\\Desktop\\TTT";

//        imagesToPdf(outPdfPath, fs);
        createPDF(outPdfPath, "C:\\Users\\duiya\\Desktop\\TTT");
    }
}
