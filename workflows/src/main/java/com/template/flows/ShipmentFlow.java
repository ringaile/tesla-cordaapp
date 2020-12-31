package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.CarContract;
import com.template.contracts.TemplateContract;
import com.template.states.CarState;
import com.template.states.TemplateState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.template.contracts.CarContract.CAR_CONTRACT_ID;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class ShipmentFlow extends FlowLogic<SignedTransaction> {

    // We will not use these ProgressTracker for this Hello-World sample
    private final ProgressTracker progressTracker = new ProgressTracker();
    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    //private variables
    private String model;
    private Party owner ;

    //public constructor
    public ShipmentFlow(String model, Party owner){
        this.model = model;
        this.owner = owner;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        // Retrieve the notary identity from the network map
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        // Create the transaction arguments (inputs/outputs)
        CarState outputState = new CarState(model, owner, getOurIdentity());

        // Create the transaction builder an add components
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(outputState, CAR_CONTRACT_ID)
                .addCommand(new CarContract.Commands.Shipment(), getOurIdentity().getOwningKey());

        // Signing the transaction
        SignedTransaction shipmentTx = getServiceHub().signInitialTransaction(txBuilder);

        // Create session with counterparty
        FlowSession otherPartySession = initiateFlow(owner);

        // Finalizing the transaction
        return subFlow(new FinalityFlow(shipmentTx, otherPartySession));
    }
}
