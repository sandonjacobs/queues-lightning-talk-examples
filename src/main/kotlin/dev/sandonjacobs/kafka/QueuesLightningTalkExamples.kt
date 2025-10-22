package dev.sandonjacobs.kafka

import dev.sandonjacobs.kafka.example1.Example1Coordinator
import org.slf4j.LoggerFactory

/**
 * Main class for the Queues Lightning Talk Examples application.
 * This application demonstrates various Kafka queue patterns and examples.
 * 
 * @author Sandon Jacobs
 * @version 1.0.0-SNAPSHOT
 */
object QueuesLightningTalkExamples {
    
    private val logger = LoggerFactory.getLogger(QueuesLightningTalkExamples::class.java)
    
    /**
     * Main entry point for the application.
     * 
     * @param args command line arguments
     */
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Starting Queues Lightning Talk Examples application")
        
        try {
            // TODO: Add example implementations here
            logger.info("Application started successfully")
            
            // For now, just demonstrate that the application can start
            Thread(Example1Coordinator()).start()

        } catch (e: Exception) {
            logger.error("Error starting application", e)
            System.exit(1)
        }
        
        logger.info("Application completed successfully")
    }

}
