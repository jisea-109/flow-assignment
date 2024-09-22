package com.flow.flow_assignment;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FlowController {
	
	private Set<String> fixedExtensions = new HashSet<>(7); // 고정 확장자
    private Set<String> addedExtensions = new HashSet<>(200); // 커스텀 확장자
    private Set<String> selectedExtensions = new HashSet<>(207); // 확장자 차단 목록

    public FlowController() { // 고정 확장자 리스트
        fixedExtensions.add("bat");
        fixedExtensions.add("cmd");
        fixedExtensions.add("com");
        fixedExtensions.add("cpl");
        fixedExtensions.add("exe");
        fixedExtensions.add("scr");
        fixedExtensions.add("js");
    }

	// 첫 번째 페이지: 파일 업로드 페이지
	@GetMapping("/upload")
		public String showUploadForm(Model model) {
		model.addAttribute("selectedExtensions", selectedExtensions); // 제한된 확장자 html로 전달
		return "upload";
	}

 
	// 파일 업로드 처리, 저장은 미구현
	@PostMapping("/upload")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
		String fileName = file.getOriginalFilename();

		if (fileName != null) {
			// 파일 확장자 추출
			String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			String mimeType = file.getContentType();
			// 파일 확장자가 확장자 리스트에 있는지 확인
			if (selectedExtensions.contains(fileExtension)) {
				model.addAttribute("message", "업로드가 제한된 파일 유형입니다: " + fileExtension);
			}
			else if (!isValidMimeType(mimeType)) {
				model.addAttribute("message", "지원하지 않는 파일 형식입니다: " + mimeType);
				return "upload";
			}
			else { // 파일 업로드 성공 처리 (업로드 구현 X)
			model.addAttribute("message", "파일 업로드 성공: " + fileName);
			}
		}
		model.addAttribute("selectedExtensions", selectedExtensions);
		return "upload";
	}

	// 두 번째 페이지: 확장자 추가 및 선택 페이지
	@GetMapping("/select-extension")
	public String showExtensionForm(Model model) {
		model.addAttribute("fixedExtensions", fixedExtensions); // 고정 확장자
		model.addAttribute("addedExtensions", addedExtensions); // 추가된 확장자
		model.addAttribute("selectedExtensions", selectedExtensions); // 확장자 차단 목록
		return "extension";
	}

	// 파일 확장자 선택 처리 (체크박스로 선택된 확장자 차단)
	@PostMapping("/select-extension")
	public String handleSelectedExtensions( 
		@RequestParam(value = "fixed-selected", required = false) Set<String> fixedSelected,
		Model model) {
		selectedExtensions.clear(); // 이전에 선택된 확장자 db 초기화
		if (fixedSelected != null) {
		selectedExtensions.addAll(fixedSelected);
		}
		if (addedExtensions != null) { // 커스텀 확장자도 selectedExtensions에 다시 추가
		selectedExtensions.addAll(addedExtensions);
		}
		return "redirect:/select-extension";
	}

	// 파일 확장자 추가 처리
	@PostMapping("/add-extension")
	public String addExtension(@RequestParam("extension") String extension, RedirectAttributes redirectAttributes) {
		if (!extension.toLowerCase().matches("^[a-zA-Z0-9/]+$")) {
			redirectAttributes.addFlashAttribute("message", "확장자에는 알파벳과 숫자 그리고 '/' 만 사용할 수 있습니다.");
			return "redirect:/select-extension";
		} 
		else if (extension.toLowerCase().length() > 20) {
			redirectAttributes.addFlashAttribute("message", "확장자 최대 길이는 20자리 입니다.");
			return "redirect:/select-extension";
		}
		else if (addedExtensions.contains(extension.toLowerCase())) { // 커스텀 확장자 중복 확인
			redirectAttributes.addFlashAttribute("message", "이미 추가된 확장자입니다.");
		} 
		else if (addedExtensions.size() >= 200) {
			redirectAttributes.addFlashAttribute("message", "커스텀 확장자 최대 갯수(200개)에 도달했습니다.");
		}
		else { // 커스텀 확장자 추가
			addedExtensions.add(extension.toLowerCase());
			selectedExtensions.add(extension.toLowerCase()); // upload.html에 표시될 selectedExntensions 에도 추가
			redirectAttributes.addFlashAttribute("message", extension + " 확장자가 추가되었습니다.");
		}
		return "redirect:/select-extension";
	}

	// 선택된 커스텀 확장자 삭제 처리
	@PostMapping("/remove-extension")
	public String removeExtension(@RequestParam("extension") String extension, RedirectAttributes redirectAttributes) {
		addedExtensions.remove(extension);
		selectedExtensions.remove(extension); // upload.html에 전달되는 selectedExtension hash에서도 삭제 처리
		redirectAttributes.addFlashAttribute("message", extension + " 확장자가 삭제되었습니다.");
		return "redirect:/select-extension";
	}
	private boolean isValidMimeType(String mimeType) {
		// 허용된 MIME 타입 목록
		String[] allowedMimeTypes = {
			"image/jpeg",
			"image/png",
			"application/pdf",
			"text/plain"
			// 추가 허용 타입을 여기에 적기
		};
		for (String allowedType : allowedMimeTypes) {
			if (allowedType.equals(mimeType)) {
				return true;
			}
		}
		return false;
	}
}