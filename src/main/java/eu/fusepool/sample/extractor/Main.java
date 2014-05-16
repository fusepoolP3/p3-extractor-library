/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.fusepool.sample.extractor;

import org.eclipse.jetty.server.Server;

/**
 *
 * @author reto
 */
public class Main {
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8181);
        server.setHandler(new SimpleExtractor());
        server.start();
        server.join();
    }
}
