/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.image.viewer.sink;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 *
 * @author Christian Tzolov
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties({ ImageViewerSinkProperties.class })
public class ImageViewerSinkConfiguration {

	private static final Log logger = LogFactory.getLog(ImageViewerSinkConfiguration.class);

	@Autowired
	private ImageViewerSinkProperties properties;

	@Autowired
	private JFrame jFrame;

	@Autowired
	private DisplayUtilities.ImageComponent imageComponent;

	@Bean
	public JFrame frame(DisplayUtilities.ImageComponent imageComponent) {
		JFrame frame = DisplayUtilities.makeFrame(properties.getTitle());
		frame.add(imageComponent);
		return frame;
	}

	@Bean
	public DisplayUtilities.ImageComponent imageComponent() {
		return new DisplayUtilities.ImageComponent();
	}

	@ServiceActivator(inputChannel = Sink.INPUT)
	public void handle(Message<?> message) {

		byte[] imageBytes = (byte[]) message.getPayload();
		try {
			MBFImage image = ImageUtilities.readMBF(new ByteArrayInputStream(imageBytes));
			BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(image);
			imageComponent.setImage(bi);
			imageComponent.setOriginalImage(image);
			imageComponent.setSize(bi.getWidth(), bi.getHeight());
			imageComponent.setPreferredSize(new Dimension(imageComponent.getWidth(), imageComponent.getHeight()));


			jFrame.pack();
			jFrame.setVisible(true);
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
}
