/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.easit.core.controllers.twitter;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.easit.dao.model.PSMetadata;
import org.springframework.social.MissingAuthorizationException;
import org.springframework.social.OperationNotPermittedException;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TwitterFriendsController {

    private final Twitter twitter;

    @Inject
    public TwitterFriendsController(Twitter twitter) {
	this.twitter = twitter;
    }

    @ExceptionHandler(MissingAuthorizationException.class)
    public String handleAuthorizationException(Principal currentUser) {
	return "redirect:/connect/twitter";
    }

    @RequestMapping(value = "/twitter/friends", method = RequestMethod.GET)
    public String friends(Model model, String offset) {
	int resultLimit = 0;
	int resultOffset = 0;
	int listSize = 0;

	int int_offset = 0;
	if (offset != null) {
	    int_offset = Integer.valueOf(offset);
	}

	listSize = twitter.friendOperations().getFriends().size();

	if (listSize <= int_offset + PSMetadata.TWITTER_LIMIT_RESULT) {
	    resultLimit = listSize;
	} else {
	    resultLimit = int_offset + PSMetadata.TWITTER_LIMIT_RESULT;
	}
	if (listSize <= int_offset) {
	    resultOffset = listSize;
	} else {
	    resultOffset = int_offset;
	}
	List<TwitterProfile> friends = twitter.friendOperations().getFriends().subList(resultOffset, resultLimit);
	List<Boolean> friendship = new ArrayList<Boolean>();
	String userName = twitter.userOperations().getScreenName();
	for (TwitterProfile f : friends) {
	    friendship.add(twitter.friendOperations().friendshipExists(userName, f.getScreenName()));
	}
	model.addAttribute("profiles", friends);
	model.addAttribute("friendship", friendship);
	model.addAttribute("offset", resultOffset);
	model.addAttribute("pageSize", listSize);
	return "twitter/friends";
    }

    @RequestMapping(value = "/twitter/followers", method = RequestMethod.GET)
    public String followers(Model model, String offset) {
	int resultLimit = 0;
	int resultOffset = 0;
	int listSize = 0;

	int int_offset = 0;
	if (offset != null) {
	    int_offset = Integer.valueOf(offset);
	}

	listSize = twitter.friendOperations().getFollowers().size();

	if (listSize <= int_offset + PSMetadata.TWITTER_LIMIT_RESULT) {
	    resultLimit = listSize;
	} else {
	    resultLimit = int_offset + PSMetadata.TWITTER_LIMIT_RESULT;
	}
	if (listSize <= int_offset) {
	    resultOffset = listSize;
	} else {
	    resultOffset = int_offset;
	}

	List<TwitterProfile> followers = twitter.friendOperations().getFollowers().subList(resultOffset, resultLimit);
	List<Boolean> friendship = new ArrayList<Boolean>();
	String userName = twitter.userOperations().getScreenName();
	for (TwitterProfile f : followers) {
	    try {
		friendship.add(twitter.friendOperations().friendshipExists(userName, f.getScreenName()));
	    } catch (OperationNotPermittedException e) {
		friendship.add(false);
	    }
	}
	model.addAttribute("profiles", followers);
	model.addAttribute("friendship", friendship);
	model.addAttribute("offset", resultOffset);
	model.addAttribute("pageSize", listSize);
	return "twitter/friends";
    }

    @RequestMapping(value = "/twitter/follow/{screenName}", method = RequestMethod.POST)
    public String followUser(@PathVariable("screenName") String screenName, Model model) {
	if (twitter.friendOperations().friendshipExists(twitter.userOperations().getScreenName(), screenName)) {
	    twitter.friendOperations().unfollow(screenName);
	} else {
	    twitter.friendOperations().follow(screenName);
	}

	return "redirect:/twitter/friends";
    }

}