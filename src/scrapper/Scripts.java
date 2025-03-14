package scrapper;

public class Scripts {
	public static final String OBSERVER = """
			function noLink(userProfile) {
				if (userProfile) {
					return userProfile.replace('/pt/games/double?modal=profile_new&user_id=', '');
				}
			}
			
			function processEntry(entryData, color) {
				var user = entryData.querySelector('div a.user-profile-link').getAttribute('href');
				return [
					noLink(user),
					entryData.querySelector('div.amount').textContent.replace('R$ ', ''),
					color.replace('roulette-column', '')
				];
			}
			
			const observer = new MutationObserver((mutationsList, observer) => {
				for (let mutation of mutationsList) {
				if (mutation.type === 'childList') {
						mutation.addedNodes.forEach(node => {
							if (node.classList && node.classList.contains('entry')) {
								var color = node.parentNode.parentNode.parentNode.getAttribute('class');
								var user_amount = processEntry(node, color);
			
								if (user_amount[0]) {
									window.entriesData = window.entriesData || [];
									window.entriesData.push(user_amount);
								}
							}
						});
					}
				}
			});
			
			const config = { childList: true, subtree: true };
			observer.observe(document.body, config);
			
			return window.entriesData || [];
			""";
	
	public static final String DISABLE_OBSERVER = "observer.disconnect();";
	public static final String CLEAR_ENTRIES = "window.entriesData = [];";
}
