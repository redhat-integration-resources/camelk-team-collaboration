//Camel API
import org.apache.camel.AggregationStrategy;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

//Camel Google API
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;

//Google API
import com.google.api.services.sheets.v4.model.ValueRange;

//Java API
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HelperStage4 extends RouteBuilder {
    
    //dummy
    @Override
    public void configure() throws Exception {}


    @BindToRegistry
    public static Processor prepareGoogleParameters(){

        return new Processor() {
            public void process(Exchange exchange) throws Exception {

                //helper variables
                String row   = exchange.getProperty("row", String.class).trim();
                String range = "E"+row+":E"+row;

                //handy logs
                System.out.println("row: "+row);
                System.out.println("range: "+range);

                //encapsulate response as array of sheet values
                List<List<Object>> data =
                    Arrays.asList(
                        Arrays.asList(exchange.getIn().getBody(String.class))
                    );

                //setup Google structure
                ValueRange values = new ValueRange();
                values.setValues(data);

                //prepare Google parameters
                final Map<String, Object> headers = new HashMap<>();
                headers.put(GoogleSheetsConstants.PROPERTY_PREFIX + "range",  range);
                headers.put(GoogleSheetsConstants.PROPERTY_PREFIX + "values", values);
                headers.put(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption", "USER_ENTERED");
                exchange.getIn().setHeaders(headers);
            }
        };
    }
}