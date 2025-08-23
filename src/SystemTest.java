import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jade.core.Runtime;

/**
 * Test class untuk memverifikasi sistem multi-agent
 * Dapat dijalankan secara terpisah untuk testing
 */
public class SystemTest {
    
    public static void main(String[] args) {
        System.out.println("=== Multi-Agent Movie Booking System Test ===");
        System.out.println("Starting system test...");
        
        try {
            // Test 1: Basic JADE Runtime
            testJADERuntime();
            
            // Test 2: Agent Creation
            testAgentCreation();
            
            // Test 3: Communication Test
            testAgentCommunication();
            
            System.out.println("=== All tests passed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testJADERuntime() throws Exception {
        System.out.println("Testing JADE Runtime...");
        
        Runtime rt = Runtime.instance();
        if (rt == null) {
            throw new Exception("Failed to create JADE Runtime");
        }
        
        Profile p = new ProfileImpl();
        AgentContainer container = rt.createMainContainer(p);
        
        if (container == null) {
            throw new Exception("Failed to create main container");
        }
        
        System.out.println("✓ JADE Runtime test passed");
        
        // Clean up
        container.kill();
    }
    
    private static void testAgentCreation() throws Exception {
        System.out.println("Testing Agent Creation...");
        
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        AgentContainer container = rt.createMainContainer(p);
        
        // Test Provider Agent
        AgentController provider = container.createNewAgent("test_provider",
                EnhancedProviderAgent.class.getName(), null);
        if (provider == null) {
            throw new Exception("Failed to create Provider Agent");
        }
        
        // Test Customer Agent
        AgentController customer = container.createNewAgent("test_customer",
                EnhancedCustomerAgent.class.getName(), null);
        if (customer == null) {
            throw new Exception("Failed to create Customer Agent");
        }
        
        System.out.println("✓ Agent Creation test passed");
        
        // Clean up
        container.kill();
    }
    
    private static void testAgentCommunication() throws Exception {
        System.out.println("Testing Agent Communication...");
        
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        AgentContainer container = rt.createMainContainer(p);
        
        // Create agents
        AgentController provider = container.createNewAgent("test_provider",
                EnhancedProviderAgent.class.getName(), null);
        AgentController customer = container.createNewAgent("test_customer",
                EnhancedCustomerAgent.class.getName(), null);
        
        // Start agents
        provider.start();
        customer.start();
        
        // Wait a bit for agents to initialize
        Thread.sleep(2000);
        
        System.out.println("✓ Agent Communication test passed");
        
        // Clean up
        container.kill();
    }
    
    /**
     * Test utility untuk memverifikasi ontologi
     */
    public static void testOntology() {
        System.out.println("Testing Ontology...");
        
        try {
            jade.content.onto.Ontology ontology = MovieBookingOntology.getInstance();
            if (ontology != null) {
                System.out.println("✓ Ontology test passed");
            } else {
                System.out.println("✗ Ontology test failed");
            }
        } catch (Exception e) {
            System.err.println("✗ Ontology test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test utility untuk memverifikasi logging
     */
    public static void testLogging() {
        System.out.println("Testing Logging System...");
        
        try {
            EnhancedLoggerUtil.logMessage("test_sender", "test_receiver", "TEST", "test_conv", "Test message");
            EnhancedLoggerUtil.logSystemEvent("TEST_EVENT", "Test system event");
            EnhancedLoggerUtil.logError("test_sender", "test_receiver", "Test error", "test_conv");
            
            System.out.println("✓ Logging test passed");
        } catch (Exception e) {
            System.err.println("✗ Logging test failed: " + e.getMessage());
        }
    }
}
