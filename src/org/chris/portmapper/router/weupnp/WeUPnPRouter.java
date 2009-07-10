/**
 * 
 */
package org.chris.portmapper.router.weupnp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chris.portmapper.model.PortMapping;
import org.chris.portmapper.model.Protocol;
import org.chris.portmapper.router.AbstractRouter;
import org.chris.portmapper.router.RouterException;
import org.wetorrent.upnp.GatewayDevice;
import org.wetorrent.upnp.PortMappingEntry;
import org.wetorrent.upnp.WeUPnPException;

/**
 * @author chris
 * 
 */
public class WeUPnPRouter extends AbstractRouter {

	private final Log logger = LogFactory.getLog(this.getClass());
	private GatewayDevice device;

	/**
	 * @param device
	 */
	WeUPnPRouter(GatewayDevice device) {
		this.device = device;
	}

	@Override
	public void addPortMapping(PortMapping mapping) throws RouterException {
		try {
			device.addPortMapping(mapping.getExternalPort(), mapping
					.getInternalPort(), mapping.getInternalClient(), mapping
					.getProtocol().getValue(), mapping.getDescription());
		} catch (WeUPnPException e) {
			throw new RouterException("Could not add portmapping", e);
		}
	}

	@Override
	public void addPortMappings(Collection<PortMapping> mappings)
			throws RouterException {
		for (PortMapping mapping : mappings) {
			this.addPortMapping(mapping);
		}
	}

	@Override
	public void disconnect() {
		device = null;
	}

	@Override
	public String getExternalIPAddress() throws RouterException {
		try {
			return device.getExternalIPAddress();
		} catch (WeUPnPException e) {
			throw new RouterException("Could not get external IP address", e);
		}
	}

	@Override
	public String getInternalHostName() throws RouterException {
		try {
			return new URL(device.getPresentationURL()).getHost();
		} catch (MalformedURLException e) {
			throw new RouterException("Could not get internal port", e);
		}
	}

	@Override
	public int getInternalPort() throws RouterException {
		try {
			return new URL(device.getPresentationURL()).getPort();
		} catch (MalformedURLException e) {
			throw new RouterException("Could not get internal port", e);
		}
	}

	@Override
	public String getName() throws RouterException {
		return device.getFriendlyName();
	}

	@Override
	public Collection<PortMapping> getPortMappings() throws RouterException {
		Collection<PortMapping> mappings = new LinkedList<PortMapping>();
		boolean morePortMappings = true;
		int index = 0;
		while (morePortMappings) {
			PortMappingEntry entry = null;
			try {
				logger.debug("Getting port mapping " + index + "...");
				entry = device.getGenericPortMappingEntry(index);
				logger.debug("Got port mapping " + index + ": " + entry);
			} catch (WeUPnPException e) {
				morePortMappings = false;
				logger.debug("Got an exception for index " + index
						+ ", stop getting more mappings", e);
			}

			if (entry != null) {
				Protocol protocol = entry.getProtocol().equalsIgnoreCase("TCP") ? Protocol.TCP
						: Protocol.UDP;
				PortMapping m = new PortMapping(protocol,
						entry.getRemoteHost(), entry.getExternalPort(), entry
								.getInternalClient(), entry.getInternalPort(),
						entry.getPortMappingDescription());
				mappings.add(m);
			} else {
				logger.debug("Got null port mapping for index " + index);
			}
			index++;
		}
		return mappings;
	}

	@Override
	public long getUpTime() throws RouterException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void logRouterInfo() throws RouterException {
		Map<String, String> info = new HashMap<String, String>();
		info.put("friendlyName", device.getFriendlyName());
		info.put("manufacturer", device.getManufacturer());
		info.put("modelDescription", device.getModelDescription());

		SortedSet<String> sortedKeys = new TreeSet<String>(info.keySet());

		for (String key : sortedKeys) {
			String value = info.get(key);
			logger.info("Router Info: " + key + " \t= " + value);
		}

		logger.info("def loc " + device.getLocation());
		logger.info("device type " + device.getDeviceType());
	}

	@Override
	public void removeMapping(PortMapping mapping) throws RouterException {
		this.removePortMapping(mapping.getProtocol(), mapping.getRemoteHost(),
				mapping.getExternalPort());
	}

	@Override
	public void removePortMapping(Protocol protocol, String remoteHost,
			int externalPort) throws RouterException {
		try {
			device.deletePortMapping(externalPort, protocol.getValue());
		} catch (WeUPnPException e) {
			throw new RouterException("Could not delete port mapping", e);
		}
	}
}