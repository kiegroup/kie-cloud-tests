function getSubmitResponse() {
	var kieServerUrl = $("#kie-server-url").val();
	$.ajax({
		type : 'GET',
		url : kieServerUrl,
		dataType : 'json',
		beforeSend : function(xhr) {
			var username = $("#username").val();
			var password = $("#password").val();
			var enc = btoa(username + ":" + password);
			xhr.setRequestHeader("Authorization", "Basic " + enc);
			xhr.setRequestHeader("Content-Type", "application/json");
		},
		error : function(xhr, ajaxOptions, thrownError) {
			$('.server-content').empty();
			alert(xhr.status);
			alert(thrownError);
		}
	}).then(function(data, status, jqxhr) {
		console.log(jqxhr);
		$('.server-content').empty();
		$('.server-content').append(jqxhr.responseText);
	});
};
