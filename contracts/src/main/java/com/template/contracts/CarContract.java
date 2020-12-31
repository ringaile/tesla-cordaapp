package com.template.contracts;

import com.template.states.CarState;
import com.template.states.TemplateState;
import net.corda.core.contracts.*;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class CarContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String CAR_CONTRACT_ID = "com.template.contracts.CarContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(@NotNull LedgerTransaction tx) {

        if(tx.getCommands().size() != 1) throw new IllegalArgumentException("There can only be one command");

        Command command = tx.getCommand(0);
        CommandData commandType = command.getValue();
        List<PublicKey> requiredSigners = command.getSigners();

        if(commandType instanceof Commands.Shipment) {
            // Shipment Rules

            //Shape rules

            if(tx.getInputStates().size() != 0) {
                throw new IllegalArgumentException("There cannot be input states");
            }

            if(tx.getOutputStates().size() != 1) {
                throw new IllegalArgumentException("Only one car can be shipped at a time");
            }
            //Content rules

            ContractState outputState = tx.getOutput(0);
            if(!(outputState instanceof CarState)) {
                throw new IllegalArgumentException("Output has to be of CarState");
            }

            CarState carState = (CarState) outputState;
            if(!(carState.getModel().equals("Cybertruck"))) {
                throw new IllegalArgumentException("This is not a Cybertruck");
            }

            //Signer rules
            PublicKey manufacturerKey = carState.getManufacturer().getOwningKey();
            if (!(requiredSigners.contains(manufacturerKey))) {
                throw new IllegalArgumentException("Manufacturer must sign the transaction");
            }

        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class Shipment implements Commands {}
    }
}