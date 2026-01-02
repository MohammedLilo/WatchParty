package com.lilo.controller;

import com.lilo.model.Video;
import com.lilo.model.dto.ApiResponse;
import com.lilo.model.dto.VideoOutputDTO;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.security.AuthService;
import com.lilo.service.UserService;
import com.lilo.service.VideoService;
import com.lilo.service.FileStorageService;
import com.lilo.shared.annotations.AllowedValues;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RequestMapping("/api/v1/videos")
@RestController
@RequiredArgsConstructor
@Validated
public class VideoController extends BaseController {
	private final VideoService videoService;
	private final FileStorageService fileStorageService;
	private final UserService userService;
    private final AuthService authService;

    @Operation(deprecated = true,summary = "Stream Video File by Name",
            description = "**⚠️ IMPORTANT:** Use the static serving endpoint instead <br><br> **Note:** This endpoint streams binary video data. Successful execution (200 OK) in Swagger UI will result in an 'Unable to Display' error. Use a browser or external tool to confirm video playback. The 404 error path can be tested safely here.")
    @ApiResponses(value = {
            // SUCCESS (200 OK) - Documents the video stream
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Video stream found and returned. **⚠️ WARNING:** Do NOT execute this successfully in Swagger UI; it returns raw binary data and will result in an 'Unable to Display' error. Test in a browser or dedicated client.",
                    content = @Content(mediaType = "video/mp4")
            ),
            // ERROR (404 NOT FOUND) - Documents the JSON error structure
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Video not found.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @GetMapping(value = "/{fileName}", produces = { "video/*", MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> loadVideo(@PathVariable("fileName") String fileName) {
        System.out.println("Loading video: " + fileName);
        if (videoService.findByVideoFileName(fileName).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.withError(HttpStatus.NOT_FOUND.value(), "Video not found!"));

        Resource storedResource = fileStorageService.load(fileName);

        return ResponseEntity.ok(storedResource);
	}

    @GetMapping
    public ResponseEntity<?> listVideos(@RequestParam(name = "page", defaultValue = "0") int pageNumber,
                                        @RequestParam(name = "size", defaultValue = "6") int size,
                                        @RequestParam(name = "sortBy", defaultValue = "timestamp") @AllowedValues(values = {"timestamp","videoName"}) String sortBy) {

        Page<VideoOutputDTO> storedVideosPage = videoService.findAll(pageNumber, size, Sort.by(Order.desc(sortBy)))
                                                            .map(VideoOutputDTO::fromVideo);
        return ResponseEntity.ok(storedVideosPage);
    }

	@PostMapping
	public ResponseEntity<?> uploadVideo(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestPart MultipartFile multipartFile,
                                         @RequestPart String videoName)
            throws IOException {

        long authenticatedUserId = authService.getUserId(authorizationHeader);

        Video newVideo = videoService.save(multipartFile, authenticatedUserId, videoName);
        var response = ApiResponse.withSuccess("video uploaded successfully.");
        URI location = MvcUriComponentsBuilder
                .fromMethodCall(on(VideoController.class).loadVideo(newVideo.getVideoFileName()))
                .build()
                .toUri();
        return ResponseEntity.created(location)
                .body(response);
    }

@DeleteMapping("/{fileName}")
@ResponseBody
public ResponseEntity<?> deleteVideo(@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
                                     @PathVariable("fileName") String videoFileName) {
    long authenticatedUserId= authService.getUserId(authorizationHeader);

   TableOperationResult videoDeletionResult = videoService.deleteIfUserIsOwner(videoFileName, authenticatedUserId);
    ApiResponse<?> response;
   if(videoDeletionResult.isSuccess()) {
       response = ApiResponse.withSuccess("Video deleted successfully");
       return ResponseEntity.status(HttpStatus.OK).body(response);
   }
   response = ApiResponse.withError(videoDeletionResult.getSuggestedStatusCode(), videoDeletionResult.getErrorMessage());
   return ResponseEntity.status(videoDeletionResult.getSuggestedStatusCode()).body(response);

}

}
