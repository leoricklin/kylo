define(['angular','feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-definition.html',
            controller: "FeedDefinitionController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope, $q, AccessControlService,EntityAccessControlService, FeedService, $filter) {

        var self = this;

        /**
         * Indicates if the feed definitions may be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        this.model = FeedService.editFeedModel;
        this.editableSection = false;

        $scope.$watch(function(){
            return FeedService.editFeedModel;
        },function(newVal) {
            //only update the model if it is not set yet
            if(self.model == null) {
                self.model = angular.copy(FeedService.editFeedModel);
            }
        })


        self.editModel = {};


        this.onEdit = function(){
            //copy the model
            var copy = FeedService.editFeedModel;
            self.editModel= {};
            self.editModel.feedName = copy.feedName;
            self.editModel.systemFeedName = copy.systemFeedName;
            self.editModel.description = copy.description;
            self.editModel.templateId = copy.templateId;
            self.editModel.allowIndexing = copy.allowIndexing;
        };

        this.onCancel = function() {

        }

        this.onSave = function (ev) {
            //save changes to the model
            FeedService.showFeedSavingDialog(ev, $filter('translate')('views.feed-definition.Saving'), self.model.feedName);
            var copy = angular.copy(FeedService.editFeedModel);

            copy.feedName = self.editModel.feedName;
            copy.systemFeedName = self.editModel.systemFeedName;
            copy.description = self.editModel.description;
            copy.templateId = self.editModel.templateId;
            copy.userProperties = null;
            copy.allowIndexing = self.editModel.allowIndexing;
            //Server may have updated value. Don't send via UI.
            copy.historyReindexingStatus = undefined;

            FeedService.saveFeedModel(copy).then(function (response) {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.feedName = self.editModel.feedName;
                self.model.systemFeedName = self.editModel.systemFeedName;
                self.model.description = self.editModel.description;
                self.model.templateId = self.editModel.templateId;
                self.model.allowIndexing = self.editModel.allowIndexing;
                //Get the updated value from the server.
                self.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
            }, function (response) {
                FeedService.hideFeedSavingDialog();
                FeedService.buildErrorData(self.model.feedName, response);
                FeedService.showFeedErrorsDialog();
                //make it editable
                self.editableSection = true;
            });
        };

        //Apply the entity access permissions
        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,self.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function(access) {
            self.allowEdit = access && !self.model.view.generalInfo.disabled;
        });
    };


    angular.module(moduleName).controller('FeedDefinitionController', ["$scope","$q","AccessControlService","EntityAccessControlService","FeedService", "$filter",controller]);

    angular.module(moduleName)
        .directive('thinkbigFeedDefinition', directive);

});
