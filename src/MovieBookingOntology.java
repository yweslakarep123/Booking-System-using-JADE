import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;

public class MovieBookingOntology extends Ontology {
    public static final String ONTOLOGY_NAME = "Movie-Booking-Ontology";
    
    // Performatives
    public static final int REQUEST_INFO = 1;
    public static final int INFORM_OPTIONS = 2;
    public static final int REQUEST_BOOKING = 3;
    public static final int CONFIRM_BOOKING = 4;
    public static final int DISCONFIRM_BOOKING = 5;
    public static final int REQUEST_ALTERNATIVE = 6;
    
    // Concepts
    public static final String MOVIE_REQUEST = "MovieRequest";
    public static final String MOVIE_OPTIONS = "MovieOptions";
    public static final String BOOKING_REQUEST = "BookingRequest";
    public static final String BOOKING_RESPONSE = "BookingResponse";
    public static final String SEAT_INFO = "SeatInfo";
    
    private static Ontology instance = new MovieBookingOntology();
    
    public static Ontology getInstance() {
        return instance;
    }
    
    private MovieBookingOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());
        
        try {
            // MovieRequest Schema
            ConceptSchema cs = new ConceptSchema(MOVIE_REQUEST);
            cs.add("movieTitle", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add("date", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add("preferredTime", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add("seatClass", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add("ticketCount", (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            add(cs);
            
            // SeatInfo Schema
            cs = new ConceptSchema(SEAT_INFO);
            cs.add("seatId", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add("price", (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            cs.add("available", (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
            add(cs);
            
            // MovieOptions Schema
            cs = new ConceptSchema(MOVIE_OPTIONS);
            cs.add("showtimes", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add("availableSeats", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add("prices", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            add(cs);
            
            // BookingRequest Schema
            cs = new ConceptSchema(BOOKING_REQUEST);
            cs.add("selectedTime", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add("selectedSeats", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            add(cs);
            
            // BookingResponse Schema
            cs = new ConceptSchema(BOOKING_RESPONSE);
            cs.add("success", (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
            cs.add("message", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add("transactionId", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            add(cs);
            
        } catch (OntologyException oe) {
            oe.printStackTrace();
        }
    }
}
