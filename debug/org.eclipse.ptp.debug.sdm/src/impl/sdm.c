/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#include "config.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "sdm.h"

static int	shutting_down = 0;

static void	recv_callback(sdm_message msg);

/*
 * Set node id and the number of machines executing (including master)
 */
int
sdm_setup(int argc, char *argv[])
{
	int ch, size;
	/*
	 * Get the number of nodes
	 */
	size = -1;
	for (ch = 0; ch < argc; ch++) {
		char * arg = argv[ch];
		if (strncmp(arg, "--numnodes", 10) == 0) {
			size = (int)strtol(arg+11, NULL, 10);
			break;
		}
	}
	//size = 2;

	if(size == -1) {
		return -1;
	}

	printf("size %d\n", size);

	sdm_route_set_size(size);
	/*
	 * Set the ID of the master to the num of nodes less one
	 */
	SDM_MASTER = size - 1;

	/*
	 * Since the SDM servers will be started by the mpirun, get
	 * the ID from the environment var.
	 * Important! If the variable is not declared, then
	 * this sdm is the master.
	 */

#ifdef OMPI
	char *envval = getenv("OMPI_MCA_orte_ess_vpid");
	if(envval == NULL) {
		/*
		 * Check version 1.2
		 */
		envval = getenv("OMPI_MCA_ns_nds_vpid");
	}

	if (envval != NULL) {
		int id = strtol(envval, NULL, 10);
		sdm_route_set_id(id);
	} else {
		sdm_route_set_id(SDM_MASTER);
	}

	return 0;
#else
	return -1;
#endif
}

/*
 * Initialize the abstraction layers
 */
int
sdm_init(int argc, char *argv[])
{
	if(sdm_setup(argc, argv) < 0) {
		return -1;
	}

	if (sdm_route_init(argc, argv) < 0) {
		return -1;
	}

	if (sdm_message_init(argc, argv) < 0) {
		return -1;
	}

	if (sdm_aggregate_init(argc, argv) < 0) {
		return -1;
	}

	printf("Initialization successful\n");

	sdm_message_set_recv_callback(recv_callback);

	return 0;
}

/**
 * Finalize the abstraction layers
 */
void
sdm_finalize(void)
{
	shutting_down = 1;

	sdm_aggregate_finalize();
	sdm_route_finalize();
	sdm_message_finalize();
}

/*
 * Progress messages and the aggregation layer
 */
void
sdm_progress(void)
{
	sdm_message_progress();
	sdm_aggregate_progress();
}

/*
 * Process a received message. This implements the main communication engine
 * message processing algorithm.
 */
static void
recv_callback(sdm_message msg)
{
	if (sdm_set_contains(sdm_message_get_source(msg), SDM_MASTER)) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] got downstream message src=%s, dest=%s\n", sdm_route_get_id(),
				_set_to_str(sdm_message_get_source(msg)),
				_set_to_str(sdm_message_get_destination(msg)));

		if (shutting_down) {
			/*
			 * Stop processing downstream messages
			 */
			return;
		}

		/*
		 * Start aggregating messages
		 */
		sdm_aggregate_message(msg, SDM_AGGREGATE_DOWNSTREAM);

		/*
		 * If we are the destination, then deliver the payload
		 */
		if (sdm_set_contains(sdm_message_get_destination(msg), sdm_route_get_id())) {
			sdm_message_deliver(msg);
		}

		/*
		 * Free the message once it's been forwarded
		 */
		sdm_message_set_send_callback(msg, sdm_message_free);

		/*
		 * Now forward the message
		 */
		sdm_message_send(msg);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] got upstream message src=%s, dest=%s\n", sdm_route_get_id(),
				_set_to_str(sdm_message_get_source(msg)),
				_set_to_str(sdm_message_get_destination(msg)));

		/*
		 * Upstream messages are always aggregated
		 */
		sdm_aggregate_message(msg, SDM_AGGREGATE_UPSTREAM);
	}
}
