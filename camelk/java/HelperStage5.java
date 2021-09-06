// package org.demo;

//Camel API
import org.apache.camel.AggregationStrategy;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

//Google API
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

//PDFBox API
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

//Java API
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HelperStage5 extends RouteBuilder {
    
    //Folder destination in Google Drive
    //The folder ID is found in the browser's address bar. It looks like this:
    //https://drive.google.com/drive/u/1/folders/--here-the-folder-id-in-alphanumeric--
    private static final String FOLDER_ID = "enter-here-your-folder-id";

    //dummy
    @Override
    public void configure() throws Exception {}


    @BindToRegistry
    public static Processor prepareGoogleParameters(){

        return new Processor() {
            public void process(Exchange exchange) throws Exception {

            //prepare target folder information
            ArrayList<ParentReference> parents = new ArrayList<ParentReference>();
            ParentReference folder = new ParentReference();
            folder.setId(FOLDER_ID);
            parents.add(folder);

            //prepare file information
            File fileMetadata = new File();
            fileMetadata.setTitle("report.pdf");
            fileMetadata.setParents(parents);

            //prepare PDF input stream
            InputStreamContent isc = new InputStreamContent("application/pdf", exchange.getIn().getBody(java.io.InputStream.class));
            
            //prepare Google parameters
            final Map<String, Object> headers = new HashMap<>();
            headers.put("CamelGoogleDrive.content",      fileMetadata);
            headers.put("CamelGoogleDrive.mediaContent", isc);
            exchange.getIn().setHeaders(headers);
            }
        };
    }

    @BindToRegistry
    public static AggregationStrategy qaStrategy(){

        return new AggregationStrategy() {
            public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

                if (oldExchange == null) {

                    //create list
                    ArrayList questions = new ArrayList();

                    //add the question to the list  
                    questions.add(newExchange.getIn().getBody());

                    //keep list in exchange
                    newExchange.setProperty("questions", questions);

                  return newExchange;
                }

                //create list
                ArrayList answers = new ArrayList();
                ArrayList teams   = new ArrayList();

                System.out.println("answer added: "+newExchange.getIn().getBody());


                //add answer to the list
                answers.add(newExchange.getIn().getBody());
                teams.add(newExchange.getProperty("team", String.class));

                //keep list in exchange
                oldExchange.setProperty("answers",   answers);
                oldExchange.setProperty("teams",     teams);

                return oldExchange;
            };
        };
    }


    @BindToRegistry
    public static AggregationStrategy docStrategy(){

        return new AggregationStrategy() {
            public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

                if (oldExchange == null) {

                    ArrayList questions = new ArrayList();
                    ArrayList answers   = new ArrayList();
                    ArrayList teams     = new ArrayList();
                    
                    questions.add(newExchange.getProperty("question", String.class));
                    answers  .add(newExchange.getProperty("answer",   String.class));
                    teams    .add(newExchange.getProperty("teams",    String.class));

                    return newExchange;
                }

                ArrayList questions = oldExchange.getProperty("questions", ArrayList.class);
                ArrayList answers   = oldExchange.getProperty("answers",   ArrayList.class);
                ArrayList teams     = oldExchange.getProperty("teams",     ArrayList.class);
                
                questions.addAll(newExchange.getProperty("questions", ArrayList.class));
                answers  .addAll(newExchange.getProperty("answers",   ArrayList.class));
                teams    .addAll(newExchange.getProperty("teams",     ArrayList.class));

                return oldExchange;
            };
        };
    }


    @BindToRegistry
    public static Processor pdfGenerator(){

        return new Processor() {
            public void process(Exchange exchange) throws Exception {

                //obtain all data
                ArrayList questions = exchange.getProperty("questions", ArrayList.class);
                ArrayList answers   = exchange.getProperty("answers",   ArrayList.class);
                ArrayList teams     = exchange.getProperty("teams",   ArrayList.class);

                //Create document
                PDDocument document = new PDDocument();
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(PDType1Font.HELVETICA, 22);

                //title section
                contentStream.beginText();
                contentStream.newLineAtOffset(175, 750);
                contentStream.setLeading(14.5f);  
                contentStream.drawString("Questions from Strategy");
                contentStream.endText();

                //main body section
                contentStream.beginText();
                contentStream.newLineAtOffset(25, 600);
                contentStream.setFont(PDType1Font.HELVETICA, 12);

                //loop over the collection of questions/answers
                for (int counter = 0; counter < questions.size(); counter++) {            
                    contentStream.setNonStrokingColor(java.awt.Color.black);

                    contentStream.drawString("Question:");
                    contentStream.newLine();

                    //QUESTION
                    //count words and lengths to ensure lines fit the document's width
                    String[] words = (((String)questions.get(counter))).split(" ");
                    int lineLength = 0;
                    for(int i=0; i<words.length; i++){

                        contentStream.drawString(words[i]+" ");
                        lineLength += words[i].length()  + 1;

                        if(lineLength > 90){
                            contentStream.newLine();
                            lineLength = 0;
                        }
                    }

                    contentStream.newLine();
                    contentStream.newLine();
                    contentStream.setNonStrokingColor(java.awt.Color.blue);
                    contentStream.drawString((String)teams.get(counter)+":");
                    contentStream.newLine();

                    //ANSWER
                    //count words and lengths to ensure lines fit the document's width
                    words = (((String)answers.get(counter))).split(" ");
                    lineLength = 0;
                    for(int i=0; i<words.length; i++){

                        contentStream.drawString(words[i]+" ");
                        lineLength += words[i].length()  + 1;

                        if(lineLength > 90){
                            contentStream.newLine();
                            lineLength = 0;
                        }
                    }


                    contentStream.newLine();
                    contentStream.newLine();
                    contentStream.newLine();
                }   

                contentStream.endText();
                contentStream.close();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                document.save(byteArrayOutputStream);
                document.close();

                exchange.getIn().setBody(byteArrayOutputStream);
            }
        };
    }
}