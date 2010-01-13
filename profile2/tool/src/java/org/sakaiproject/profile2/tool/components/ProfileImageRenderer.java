/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.util.ProfileConstants;

/** 
 * This is a helper panel for displaying a user's profile image.
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileImageRenderer extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Render a profile image for a user, based on the settings supplied
	 * @param id		- wicket:id
	 * @param userX		- user whose image we are showing
	 * @param allowed	- if this image is allowed to be viewed by the person requesting it. If false, a default image will be used.
	 * @param size		- ProfileImageManager.PROFILE_IMAGE_MAIN, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL
	 * @param cacheable	- if this image is allowed to be cached or not. If having issues with sticky images from AJAX updates, set this to false to ensure the image is updated every request
	 */
	public ProfileImageRenderer(String id, String userX, boolean allowed, int size, boolean cacheable) {
		super(id);
		
		//if we aren't allowed to view it, no use processing, just show default
		if(!allowed) {
			add(new ContextImage("img",new Model<String>(getDefaultImage())));
			return;
		}
		
		//get API's
        SakaiProxy sakaiProxy = getSakaiProxy();
        ProfileLogic profileLogic = getProfileLogic();
                
		//what type of image are we to show?
		int type = sakaiProxy.getProfilePictureType();
		
		//UPLOAD
		if(type == ProfileConstants.PICTURE_SETTING_UPLOAD) {

			final byte[] bytes = profileLogic.getCurrentProfileImageForUser(userX, size);
			
			//use profile bytes or add default image if none
			if(bytes != null && bytes.length > 0){
			
				BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
					private static final long serialVersionUID = 1L;

					protected byte[] getImageData() {
						return bytes;
					}
				};
				if(cacheable) {
					add(new Image("img",photoResource));
				} else {
					add(new NonCachingImage("img",photoResource));
				}
			} else {
				add(new ContextImage("img",new Model<String>(getDefaultImage())));
			}
		
		//EXTERNAL IMAGE
		} else if (type == ProfileConstants.PICTURE_SETTING_URL) {
			
			String url = profileLogic.getExternalImageUrl(userX, size);
			
			//add uploaded image or default
			if(url != null) {
				add(new ExternalImage("img",url));
			} else {
				add(new ContextImage("img",new Model<String>(getDefaultImage())));
			}
			
		//INVALID TYPE, SHOW DEFAULT
		} else {
			add(new ContextImage("img",new Model<String>(getDefaultImage())));
		}
		
	}
	
	
	/**
	 * Get the default image to be used if not allowed or none available.
	 * @return
	 */
	private String getDefaultImage() {
		return ProfileConstants.UNAVAILABLE_IMAGE;
	}
	
	/**
	 * API helper methods
	 * @return
	 */
	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}

	private ProfileLogic getProfileLogic() {
		return Locator.getProfileLogic();
	}
	
	
}
